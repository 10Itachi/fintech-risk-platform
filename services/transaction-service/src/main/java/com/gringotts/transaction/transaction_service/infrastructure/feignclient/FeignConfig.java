package com.gringotts.transaction.transaction_service.infrastructure.feignclient;

import feign.Logger;
import feign.Request;
import org.springframework.context.annotation.Bean;

public class FeignConfig {
    @Bean
    Logger.Level feignLoggerLevel() {
        return Logger.Level.BASIC;
    }

    @Bean
    public Request.Options feignRequestOptions() {
        return new Request.Options(
                // see the approval needs to be done in milli sec
                2000,
                2000
        );
    }
}
