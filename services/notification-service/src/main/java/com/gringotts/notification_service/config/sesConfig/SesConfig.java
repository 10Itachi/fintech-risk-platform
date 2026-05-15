package com.gringotts.notification_service.config.sesConfig;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ses.SesClient;

@Configuration
public class SesConfig {

    @Bean
    public SesClient sesClient() {

        return SesClient.builder()
                .region(Region.AP_SOUTH_1)
                .build();
    }
}