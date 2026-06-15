package com.gringotts.transaction.transaction_service.infrastructure.keycloak;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// Feign interceptor that injects service JWT into every outgoing request.
// Automatically triggered before each Feign call (no manual invocation required).
// Fetches token from ServiceTokenGenerator and adds Authorization header.
// Enables secure service-to-service communication transparently across all Feign clients.
@Configuration
@RequiredArgsConstructor
public class ServiceAuthFeignInterceptor {

    private final ServiceTokenGenerator tokenGenerator;

    @Bean
    public RequestInterceptor requestInterceptor() {

        return new RequestInterceptor() {

            @Override
            public void apply(RequestTemplate template) {

                String token = tokenGenerator.getToken();

                template.header("Authorization", "Bearer " + token);
            }
        };
    }
}
