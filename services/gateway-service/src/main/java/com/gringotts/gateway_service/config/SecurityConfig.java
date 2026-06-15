package com.gringotts.gateway_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;

import org.springframework.security.config.web.server.ServerHttpSecurity;

import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;

import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;
import org.springframework.security.web.server.header.XFrameOptionsServerHttpHeadersWriter;

import java.time.Duration;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class SecurityConfig {

    private final JwtAuthConverter jwtAuthConverter;

    public SecurityConfig(JwtAuthConverter jwtAuthConverter) {
        this.jwtAuthConverter = jwtAuthConverter;
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {

        http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)

                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)

                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .securityContextRepository(
                        NoOpServerSecurityContextRepository.getInstance()
                )
                .headers(headers -> headers

                        // HSTS
                        .hsts(hsts -> hsts
                                .includeSubdomains(true)
                                .maxAge(Duration.ofDays(365))
                        )

                        // X-Frame-Options
                        .frameOptions(frameOptions ->
                                frameOptions.mode(
                                        XFrameOptionsServerHttpHeadersWriter.Mode.DENY
                                )
                        )

                        // X-Content-Type-Options
                        .contentTypeOptions(Customizer.withDefaults())

                        // Cache-Control
                        .cache(Customizer.withDefaults())
                )

                .authorizeExchange(exchange -> exchange

                        // PUBLIC ENDPOINTS
                        .pathMatchers(
                                "/actuator/health",
                                "/actuator/health/**"
                        ).permitAll()

                        // ADMIN ONLY ROUTES
                        .pathMatchers(
                                "/api/v1/users/admin/**",
                                "/api/v1/transactions/admin/**",
                                "/risk/api/**",
                                "/api/v1/observability/**",
                                "/api/v1/notifications/**"
                        ).hasRole("ADMIN")

                        //USER ONLY
                        .pathMatchers(
                                "/api/v1/users/user/**",
                                "/api/v1/transactions/user/**"
                        ).hasRole("USER")

                        // AUTHENTICATED ANY VALID ROUTES
                        .pathMatchers(
                                "/api/v1/users/shared/**",
                                "/api/v1/transactions/shared/**"
                        ).authenticated()

                        // EVERYTHING ELSE BLOCKED
                        .anyExchange().denyAll()
                )

                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(
                                new ReactiveJwtAuthenticationConverterAdapter(
                                        jwtAuthConverter
                                )
                        ))
                );

        return http.build();
    }
}