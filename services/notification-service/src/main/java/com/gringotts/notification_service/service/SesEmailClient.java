package com.gringotts.notification_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.ses.SesClient;

@Service
@Slf4j
@RequiredArgsConstructor
public class SesEmailClient {

    private final SesClient sesClient;

    public void sendEmail(
            String to,
            String subject,
            String body
    ) {

        /*
         =====================================================
         AWS SES IMPLEMENTATION
         =====================================================

         Uncomment after AWS setup
         */

        /*
        SendEmailRequest request =
                SendEmailRequest.builder()
                        .destination(
                                Destination.builder()
                                        .toAddresses(to)
                                        .build()
                        )
                        .message(
                                Message.builder()
                                        .subject(
                                                Content.builder()
                                                        .data(subject)
                                                        .build()
                                        )
                                        .body(
                                                Body.builder()
                                                        .text(
                                                                Content.builder()
                                                                        .data(body)
                                                                        .build()
                                                        )
                                                        .build()
                                        )
                                        .build()
                        )
                        .source("your-verified-email@gmail.com")
                        .build();

        sesClient.sendEmail(request);

        log.info(
                "event=SES_EMAIL_SENT to={}",
                to
        );
        */

        /*
         =====================================================
         TEMP MOCK IMPLEMENTATION
         =====================================================
         */

        log.info(
                """
                event=MOCK_EMAIL_SENT
                to={}
                subject={}₹
                body={}
                """,
                to,
                subject,
                body
        );
    }
}