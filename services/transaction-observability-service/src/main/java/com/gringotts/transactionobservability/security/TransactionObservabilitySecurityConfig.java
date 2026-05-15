package com.gringotts.transactionobservability.security;


import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;

@Configuration
@Slf4j
public class TransactionObservabilitySecurityConfig {

    private final JwtAuthConverter jwtAuthenticationConverter;

    public TransactionObservabilitySecurityConfig(JwtAuthConverter jwtAuthenticationConverter) {
        this.jwtAuthenticationConverter = jwtAuthenticationConverter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                .authorizeHttpRequests(auth -> auth

                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/actuator/health",
                                "/actuator/metrics"
                        ).permitAll()

                        .requestMatchers("/observability/**")
                        .hasRole("ADMIN")

                        .anyRequest()
                        .authenticated()
                )

                .oauth2ResourceServer(oauth ->
                oauth.jwt(jwt -> {jwt.jwtAuthenticationConverter(jwtAuthenticationConverter);
                })
        )

                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authenticationEntryPoint())
                        .accessDeniedHandler(accessDeniedHandler())
                );

        return http.build();
    }

    private AuthenticationEntryPoint authenticationEntryPoint() {

        return (request, response, authException) -> {

            log.error(
                    "Unauthorized access attempt: {}",
                    authException.getMessage()
            );

            response.sendError(
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "Invalid or expired token"
            );
        };
    }

    private AccessDeniedHandler accessDeniedHandler() {

        return (request, response, accessDeniedException) -> {

            log.warn(
                    "Access denied for request {}: {}",
                    request.getRequestURI(),
                    accessDeniedException.getMessage()
            );

            response.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    "Insufficient permissions"
            );
        };
    }
}
