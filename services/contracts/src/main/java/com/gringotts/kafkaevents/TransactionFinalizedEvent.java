package com.gringotts.kafkaevents;


import com.gringotts.enums.TransactionStatus;
import lombok.*;


import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TransactionFinalizedEvent{
    private UUID eventId;
    private String eventType;
    private int eventVersion;
    private Instant occurredAt;

    private UUID transactionId;
    private String userId;
    private String userName;
    private String email;
    private BigDecimal amount;
    //private String status;
    private TransactionStatus finalStatus;
    private String correlationId;
}