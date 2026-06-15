package com.gringotts.transaction.transaction_service.infrastructure.keycloak;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate keycloakRestTemplate() {

        SimpleClientHttpRequestFactory factory =
                new SimpleClientHttpRequestFactory();

        // Connection timeout
        factory.setConnectTimeout(2000);

        // Read timeout
        factory.setReadTimeout(3000);

        return new RestTemplate(factory);
    }
}