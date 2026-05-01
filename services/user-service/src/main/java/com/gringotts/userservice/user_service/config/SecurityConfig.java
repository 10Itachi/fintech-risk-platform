package com.gringotts.userservice.user_service.config;

import com.gringotts.userservice.user_service.config.KeycloakJwtConverter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * SECURITY PERIMETER CONFIGURATION
 * --------------------------------
 * Configures the microservice as an OAuth2 Resource Server.
 * - Defines stateless session management (JWT-based).
 * - Sets up access rules (Permit Swagger/Health, Protect everything else).
 * - Plugs in the custom KeycloakJwtConverter to handle role mapping.
 * - Provides clean JSON error responses for Unauthorized (401) and Forbidden (403) states.
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final KeycloakJwtConverter keycloakJwtConverter;

    public SecurityConfig(KeycloakJwtConverter keycloakJwtConverter) {
        this.keycloakJwtConverter = keycloakJwtConverter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                // 1) This is a stateless API (JWT), so CSRF is not needed
                .csrf(csrf -> csrf.disable())

                // 2) No HTTP session; every request must carry its own JWT
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // 3) Centralized auth rules
                .authorizeHttpRequests(auth -> auth
                        // (Optional) allow health/actuator if you use them
                        .requestMatchers("/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/actuator/health",
                                "/actuator/**").permitAll()
                        .requestMatchers("/actuator/**").hasRole("ADMIN")
                        // All endpoints require authentication
                        .anyRequest().authenticated()
                )

                // 4) JWT resource server (Keycloak)
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .jwtAuthenticationConverter(keycloakJwtConverter)
                        )
                        // 5) Custom error handling (important for APIs)
                        .authenticationEntryPoint((req, res, ex) -> {
                            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            res.setContentType("application/json");
                            res.getWriter().write("{\"message\":\"Unauthorized\"}");
                        })
                        .accessDeniedHandler((req, res, ex) -> {
                            res.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            res.setContentType("application/json");
                            res.getWriter().write("{\"message\":\"Forbidden\"}");
                        })
                );

        return http.build();
    }
}