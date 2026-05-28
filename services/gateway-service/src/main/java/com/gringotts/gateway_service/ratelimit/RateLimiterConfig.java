package com.gringotts.gateway_service.ratelimit;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import reactor.core.publisher.Mono;

@Configuration
public class RateLimiterConfig {

    @Bean
    @Primary
    public KeyResolver userKeyResolver() {

        return exchange ->
                exchange.getPrincipal()
                        .map(principal -> principal.getName())
                        .defaultIfEmpty("anonymous");
    }

    @Bean
    public KeyResolver ipKeyResolver() {

        return exchange -> {

            String ip =
                    exchange.getRequest()
                            .getRemoteAddress()
                            .getAddress()
                            .getHostAddress();

            return Mono.just(ip);
        };
    }
}