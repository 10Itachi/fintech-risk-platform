package com.gringotts.transaction.transaction_service.infrastructure.keycloak;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

// Binds Keycloak configuration (token URL, clientId, clientSecret) from application.yml.
// Used by ServiceTokenGenerator to authenticate with Keycloak.
// Centralizes service credentials for client-credentials OAuth2 flow.
// Loaded once at startup and injected wherever needed.
@Configuration
@ConfigurationProperties(prefix = "keycloak")
@Getter
@Setter
public class ServiceJwtProperties {

    private String tokenUrl;
    private String clientId;
    private String clientSecret;
}
