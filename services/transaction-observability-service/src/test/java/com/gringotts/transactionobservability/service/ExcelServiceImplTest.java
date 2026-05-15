package com.gringotts.transactionobservability.service;

import com.gringotts.transactionobservability.domain.model.TransactionEventRecord;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class ExcelServiceImplTest {

    private ExcelServiceImpl service;

    private final SimpleMeterRegistry meterRegistry =
            new SimpleMeterRegistry();

    @BeforeEach
    void setUp() {

        service = new ExcelServiceImpl(
                meterRegistry
        );
    }

    /*
     =========================================================
     SUCCESSFUL EXCEL GENERATION
     =========================================================
     */
    @Test
    @DisplayName("Should generate valid Excel file")
    void shouldGenerateValidExcelFile()
            throws Exception {

        TransactionEventRecord record =
                TransactionEventRecord.builder()
                        .transactionId(UUID.randomUUID())
                        .eventId(UUID.randomUUID())
                        .eventType("TRANSACTION_FINALIZED")
                        .eventVersion(1)
                        .occurredAt(Instant.now())
                        .recordedAt(Instant.now())
                        .sourceService("transaction-service")
                        .checksum("abc123")
                        .build();

        byte[] file =
                service.generateTransactionExcel(
                        List.of(record)
                );

        assertThat(file)
                .isNotNull()
                .isNotEmpty();

        /*
         =====================================================
         VERIFY WORKBOOK CONTENT
         =====================================================
         */
        try (XSSFWorkbook workbook =
                     new XSSFWorkbook(
                             new ByteArrayInputStream(file)
                     )) {

            Sheet sheet =
                    workbook.getSheet(
                            "transaction-report"
                    );

            assertThat(sheet)
                    .isNotNull();

            /*
             =================================================
             HEADER ROW
             =================================================
             */
            Row header =
                    sheet.getRow(0);

            assertThat(
                    header.getCell(0)
                            .getStringCellValue()
            ).isEqualTo("TransactionId");

            assertThat(
                    header.getCell(1)
                            .getStringCellValue()
            ).isEqualTo("EventId");

            /*
             =================================================
             DATA ROW
             =================================================
             */
            Row dataRow =
                    sheet.getRow(1);

            assertThat(dataRow)
                    .isNotNull();

            assertThat(
                    dataRow.getCell(2)
                            .getStringCellValue()
            ).isEqualTo("TRANSACTION_FINALIZED");
        }

        /*
         =====================================================
         METRICS
         =====================================================
         */
        assertThat(
                meterRegistry
                        .counter("transaction.observability.excel.generated")
                        .count()
        ).isEqualTo(1.0);

        assertThat(
                meterRegistry
                        .summary("transaction.observability.excel.rows")
                        .count()
        ).isEqualTo(1);

        assertThat(
                meterRegistry
                        .summary("transaction.observability.excel.bytes")
                        .totalAmount()
        ).isGreaterThan(0);
    }

    /*
     =========================================================
     EMPTY DATASET
     =========================================================
     */
    @Test
    @DisplayName("Should generate Excel for empty dataset")
    void shouldGenerateExcelForEmptyDataset()
            throws Exception {

        byte[] file =
                service.generateTransactionExcel(
                        List.of()
                );

        assertThat(file)
                .isNotNull()
                .isNotEmpty();

        try (XSSFWorkbook workbook =
                     new XSSFWorkbook(
                             new ByteArrayInputStream(file)
                     )) {

            Sheet sheet =
                    workbook.getSheet(
                            "transaction-report"
                    );

            assertThat(sheet)
                    .isNotNull();

            /*
             =================================================
             ONLY HEADER ROW SHOULD EXIST
             =================================================
             */
            assertThat(sheet.getLastRowNum())
                    .isEqualTo(0);
        }

        assertThat(
                meterRegistry
                        .counter("transaction.observability.excel.generated")
                        .count()
        ).isEqualTo(1.0);
    }

    /*
     =========================================================
     NULL FIELD HANDLING
     =========================================================
     */
    @Test
    @DisplayName("Should handle null fields safely")
    void shouldHandleNullFieldsSafely()
            throws Exception {

        TransactionEventRecord record =
                TransactionEventRecord.builder()
                        .transactionId(null)
                        .eventId(null)
                        .eventType(null)
                        .eventVersion(0)
                        .occurredAt(null)
                        .recordedAt(null)
                        .sourceService(null)
                        .checksum(null)
                        .build();

        byte[] file =
                service.generateTransactionExcel(
                        List.of(record)
                );

        assertThat(file)
                .isNotEmpty();

        try (XSSFWorkbook workbook =
                     new XSSFWorkbook(
                             new ByteArrayInputStream(file)
                     )) {

            Sheet sheet =
                    workbook.getSheet(
                            "transaction-report"
                    );

            Row row =
                    sheet.getRow(1);

            assertThat(row)
                    .isNotNull();

            /*
             =================================================
             NULLS SHOULD BE WRITTEN AS EMPTY STRINGS
             =================================================
             */
            assertThat(
                    row.getCell(0)
                            .getStringCellValue()
            ).isEmpty();

            assertThat(
                    row.getCell(2)
                            .getStringCellValue()
            ).isEmpty();
        }
    }

    /*
     =========================================================
     MULTIPLE RECORDS
     =========================================================
     */
    @Test
    @DisplayName("Should generate Excel for multiple records")
    void shouldGenerateExcelForMultipleRecords()
            throws Exception {

        List<TransactionEventRecord> records =
                List.of(
                        buildRecord(),
                        buildRecord(),
                        buildRecord()
                );

        byte[] file =
                service.generateTransactionExcel(
                        records
                );

        assertThat(file)
                .isNotEmpty();

        try (XSSFWorkbook workbook =
                     new XSSFWorkbook(
                             new ByteArrayInputStream(file)
                     )) {

            Sheet sheet =
                    workbook.getSheet(
                            "transaction-report"
                    );

            /*
             =================================================
             HEADER + 3 DATA ROWS
             =================================================
             */
            assertThat(sheet.getLastRowNum())
                    .isEqualTo(3);
        }

        assertThat(
                meterRegistry
                        .summary("transaction.observability.excel.rows")
                        .totalAmount()
        ).isEqualTo(3.0);
    }

    /*
     =========================================================
     TEST DATA
     =========================================================
     */
    private TransactionEventRecord buildRecord() {

        return TransactionEventRecord.builder()
                .transactionId(UUID.randomUUID())
                .eventId(UUID.randomUUID())
                .eventType("TRANSACTION_FINALIZED")
                .eventVersion(1)
                .occurredAt(Instant.now())
                .recordedAt(Instant.now())
                .sourceService("transaction-service")
                .checksum("checksum")
                .build();
    }
}