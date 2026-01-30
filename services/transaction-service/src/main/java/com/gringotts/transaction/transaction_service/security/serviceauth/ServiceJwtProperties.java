package com.gringotts.transaction.transaction_service.security.serviceauth;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "service.jwt")
@Data
@Getter
@Setter
public class ServiceJwtProperties {
    private String secret;
    private String issuer;
    private String audience;
    private  long ttlSeconds;
}
