package com.gringotts.transaction.transaction_service.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;


// Defines the Spring Security filter chain; invoked automatically at application startup to configure request security.
// Registers filters (like BearerTokenAuthenticationFilter) that intercept every incoming request.
// On each request: extracts JWT → validates it → applies JwtAuthConverter → stores Authentication in SecurityContext.
// After this, the request proceeds to controller/service with authenticated user context available.
@Configuration
@EnableMethodSecurity
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthConverter jwtAuthConverter;

    public SecurityConfig(JwtAuthConverter jwtAuthConverter) {
        this.jwtAuthConverter = jwtAuthConverter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf((csrf) -> csrf.disable())
                .authorizeHttpRequests(auth->auth
                        .requestMatchers("/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/actuator/health",
                                "/actuator/metrics").permitAll()
                        .requestMatchers("/actuator/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2->oauth2
                        .jwt(jwt ->jwt
                                .jwtAuthenticationConverter(jwtAuthConverter)
                        )
                );
        return http.build();
    }
}
