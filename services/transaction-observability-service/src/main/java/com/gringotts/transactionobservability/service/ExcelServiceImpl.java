package com.gringotts.transactionobservability.service;

import com.gringotts.transactionobservability.domain.model.TransactionEventRecord;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@Slf4j
public class ExcelServiceImpl implements ExcelService {

    /*
     =========================================================
     CONSTANTS
     =========================================================
     */
    private static final DateTimeFormatter ISO =
            DateTimeFormatter.ISO_INSTANT;

    /*
     =========================================================
     STREAMING WINDOW SIZE
     =========================================================

     Limits in-memory rows.
     Prevents large heap spikes.
     */
    private static final int ROW_ACCESS_WINDOW_SIZE = 100;

    /*
     =========================================================
     METRICS
     =========================================================
     */
    private final Counter excelGenerationSuccessCounter;
    private final Counter excelGenerationFailureCounter;

    private final Timer excelGenerationTimer;

    private final DistributionSummary excelRowsMetric;
    private final DistributionSummary excelFileSizeMetric;

    public ExcelServiceImpl(MeterRegistry meterRegistry) {

        this.excelGenerationSuccessCounter =
                meterRegistry.counter(
                        "transaction.observability.excel.generated"
                );

        this.excelGenerationFailureCounter =
                meterRegistry.counter(
                        "transaction.observability.excel.failed"
                );

        this.excelGenerationTimer =
                meterRegistry.timer(
                        "transaction.observability.excel.generation.time"
                );

        this.excelRowsMetric =
                meterRegistry.summary(
                        "transaction.observability.excel.rows"
                );

        this.excelFileSizeMetric =
                meterRegistry.summary(
                        "transaction.observability.excel.bytes"
                );
    }

    @Override
    public byte[] generateTransactionExcel(
            List<TransactionEventRecord> data
    ) {

        Timer.Sample timer = Timer.start();

        log.info(
                "event=excel_export_started rows={}",
                data.size()
        );

        /*
         =====================================================
         STREAMING WORKBOOK
         =====================================================
         */
        try (SXSSFWorkbook workbook =
                     new SXSSFWorkbook(ROW_ACCESS_WINDOW_SIZE);

             ByteArrayOutputStream out =
                     new ByteArrayOutputStream()) {

            workbook.setCompressTempFiles(true);

            Sheet sheet =
                    workbook.createSheet(
                            "transaction-report"
                    );

            /*
             =================================================
             STYLES
             =================================================
             */
            CellStyle headerStyle =
                    createHeaderStyle(workbook);

            CellStyle textStyle =
                    createTextStyle(workbook);

            /*
             =================================================
             HEADERS
             =================================================
             */
            String[] headers = {
                    "TransactionId",
                    "EventId",
                    "EventType",
                    "EventVersion",
                    "OccurredAt",
                    "RecordedAt",
                    "SourceService",
                    "Checksum"
            };

            Row headerRow = sheet.createRow(0);

            for (int i = 0; i < headers.length; i++) {

                Cell cell =
                        headerRow.createCell(i);

                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);

                /*
                 =============================================
                 FIXED WIDTHS
                 =============================================

                 Avoid expensive autoSizeColumn().
                 */
                sheet.setColumnWidth(i, 6000);
            }

            /*
             =================================================
             DATA
             =================================================
             */
            int rowIdx = 1;

            for (TransactionEventRecord record : data) {

                Row row =
                        sheet.createRow(rowIdx++);

                int col = 0;

                set(row, col++, record.getTransactionId(), textStyle);

                set(row, col++, record.getEventId(), textStyle);

                set(row, col++, record.getEventType(), textStyle);

                set(row, col++, record.getEventVersion(), textStyle);

                set(row, col++, toIso(record.getOccurredAt()), textStyle);

                set(row, col++, toIso(record.getRecordedAt()), textStyle);

                set(row, col++, record.getSourceService(), textStyle);

                set(row, col++, record.getChecksum(), textStyle);
            }

            /*
             =================================================
             WRITE
             =================================================
             */
            workbook.write(out);

            byte[] file =
                    out.toByteArray();

            /*
             =================================================
             METRICS
             =================================================
             */
            excelGenerationSuccessCounter.increment();

            excelRowsMetric.record(data.size());

            excelFileSizeMetric.record(file.length);

            log.info(
                    "event=excel_export_completed rows={} fileSizeBytes={}",
                    data.size(),
                    file.length
            );

            return file;

        } catch (Exception ex) {

            excelGenerationFailureCounter.increment();

            log.error(
                    "event=excel_export_failed rows={} message={}",
                    data.size(),
                    ex.getMessage(),
                    ex
            );

            throw new RuntimeException(
                    "Failed to generate transaction Excel report",
                    ex
            );

        } finally {

            timer.stop(excelGenerationTimer);
        }
    }

    /*
     =========================================================
     CELL HELPERS
     =========================================================
     */
    private void set(
            Row row,
            int col,
            Object value,
            CellStyle style
    ) {

        Cell cell = row.createCell(col);

        if (value == null) {

            cell.setCellValue("");

        } else if (value instanceof Number number) {

            cell.setCellValue(number.doubleValue());

        } else {

            cell.setCellValue(value.toString());
        }

        cell.setCellStyle(style);
    }

    /*
     =========================================================
     TIMESTAMP FORMAT
     =========================================================
     */
    private String toIso(java.time.Instant ts) {

        return ts == null
                ? ""
                : ISO.format(ts.atOffset(ZoneOffset.UTC));
    }

    /*
     =========================================================
     HEADER STYLE
     =========================================================
     */
    private CellStyle createHeaderStyle(Workbook workbook) {

        Font font =
                workbook.createFont();

        font.setBold(true);

        CellStyle style =
                workbook.createCellStyle();

        style.setFont(font);

        style.setAlignment(
                HorizontalAlignment.CENTER
        );

        return style;
    }

    /*
     =========================================================
     TEXT STYLE
     =========================================================
     */
    private CellStyle createTextStyle(Workbook workbook) {

        CellStyle style =
                workbook.createCellStyle();

        style.setAlignment(
                HorizontalAlignment.LEFT
        );

        return style;
    }
}