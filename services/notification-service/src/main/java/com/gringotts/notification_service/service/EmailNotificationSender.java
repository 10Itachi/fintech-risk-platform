package com.gringotts.notification_service.service;

import com.gringotts.kafkaevents.TransactionFinalizedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailNotificationSender
        implements NotificationSender {

    private final SesEmailClient sesEmailClient;

    @Override
    public void send(
            TransactionFinalizedEvent event
    ) {

        String subject =
                "Transaction Status Update";

        String body = String.format(
                """
                Hello %s,

                Your transaction %s for amount %s is %s.

                Regards,
                Gringotts Bank
                """,

                event.getUserName(),
                event.getTransactionId(),
                event.getAmount(),
                event.getFinalStatus()
        );

        sesEmailClient.sendEmail(
                event.getEmail(),
                subject,
                body
        );
    }
}
