package com.gringotts.risk.risk_decision_service.infrastructure.security;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@ConditionalOnProperty(
        prefix = "security.jwt",
        name = "enabled",
        havingValue = "true"
)
public class RiskServiceSecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/risk/evaluate")
                        .hasAuthority("SCOPE_RISK_EVALUATE")
                        .anyRequest().denyAll()
                )
                .oauth2ResourceServer(oauth -> oauth.jwt());

        return http.build();
    }
}
