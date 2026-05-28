package com.gringotts.risk.risk_decision_service.infrastructure.security;

import lombok.extern.slf4j.Slf4j; // Using Lombok for easy logging
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.oauth2.core.*;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

import jakarta.servlet.http.HttpServletResponse;

@Configuration
@Slf4j // 1. Adds 'log' variable automatically
public class RiskServiceSecurityConfig {

    private final JwtAuthConverter jwtAuthConverter;

    public RiskServiceSecurityConfig(JwtAuthConverter jwtAuthConverter) {
        this.jwtAuthConverter = jwtAuthConverter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        log.info("Initializing Risk Service Security Configuration");

        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/actuator/**").permitAll()
                        .requestMatchers("/risk/evaluate").hasRole("RISKCALLER")
                        .requestMatchers("/risk/api/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth -> oauth
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthConverter))
                        // 2. Custom Error Handling for Authentication (Token fails)
                        .authenticationEntryPoint(authenticationEntryPoint())
                )
                // 3. Custom Error Handling for Authorization (Roles fail)
                .exceptionHandling(ex -> ex.accessDeniedHandler(accessDeniedHandler()));

        return http.build();
    }

    //this  decoder is called and its default and custom validator are handled automatically by spring security
    @Bean
    public JwtDecoder jwtDecoder(
            @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
            String issuerUri,

            @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}")
            String jwkSetUri) {

        log.info("Configuring JwtDecoder with JWK Set URI: {}", jwkSetUri);

        NimbusJwtDecoder jwtDecoder =
                NimbusJwtDecoder
                        .withJwkSetUri(jwkSetUri)
                        .build();

        OAuth2TokenValidator<Jwt> defaultValidator =
                JwtValidators.createDefaultWithIssuer(issuerUri);

        OAuth2TokenValidator<Jwt> customValidator =
                new CustomJwtValidator();

        OAuth2TokenValidator<Jwt> combinedValidator =
                new DelegatingOAuth2TokenValidator<>(
                        defaultValidator,
                        customValidator
                );

        jwtDecoder.setJwtValidator(combinedValidator);

        log.info("JwtDecoder configured successfully");

        return jwtDecoder;
    }

    // --- HELPER BEANS FOR PRODUCTION OBSERVABILITY ---

    /**
     * Handles 401 Unauthorized errors (e.g., Token expired, invalid audience)
     */
    private AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, authException) -> {
            log.debug("Unauthorized access: {}", request.getRequestURI());
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired token");
        };
    }

    /**
     * Handles 403 Forbidden errors (e.g., User is logged in but doesn't have ROLE_ADMIN)
     */
    private AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            log.warn("Access denied for request {}: {}", request.getRequestURI(), accessDeniedException.getMessage());
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Insufficient permissions");
        };
    }
}