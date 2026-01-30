package com.gringotts.risk.risk_decision_service.infrastructure.security;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Configuration
@ConditionalOnProperty(
        prefix = "security.jwt",
        name = "enabled",
        havingValue = "true"
)
public class JwtDecoderConfig {

    @Bean
    @ConditionalOnProperty("security.jwt.enabled")
    JwtDecoder jwtDecoder(ServiceJwtProperties props) {
        // Better error message for debugging
        if (props.getSecret() == null || props.getSecret().isBlank()) {
            throw new IllegalArgumentException("JWT Secret is missing! Check your application.yml under security.jwt.secret");
        }

        byte[] secret = props.getSecret().getBytes(StandardCharsets.UTF_8);
        return NimbusJwtDecoder.withSecretKey(
                new SecretKeySpec(secret, "HmacSHA256")
        ).build();
    }
}

