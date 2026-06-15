package com.gringotts.notification_service.config.security;


import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;

@Configuration
@Slf4j
public class NotificationSecurityConfig {

    private final JwtAuthConverter jwtAuthenticationConverter;

    public NotificationSecurityConfig(JwtAuthConverter jwtAuthenticationConverter) {
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

                        .requestMatchers("/api/v1/notifications/**")
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
