package com.gringotts.risk.risk_decision_service.infrastructure.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "security.jwt")
@Getter
@Setter
public class ServiceJwtProperties {

    private boolean enabled;
    @jakarta.validation.constraints.NotBlank
    private String secret;
    private String issuer;   // Added to capture the YAML value
    private String audience;
}
