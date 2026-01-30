package com.gringotts.transaction.transaction_service.config.feign;

import feign.Request;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {

    @Bean
    public Request.Options feignRequestOptions() {
        return new Request.Options(
                // see the approval needs to be done in milli sec
                2000,
                2000
        );
    }
}
