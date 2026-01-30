package com.gringotts.transaction.transaction_service.security.serviceauth;


import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Date;

@Component
public class ServiceTokenGenerator {


    private final ServiceJwtProperties serviceJwtProperties;

    public ServiceTokenGenerator(ServiceJwtProperties serviceJwtProperties) {
        this.serviceJwtProperties = serviceJwtProperties;
    }

    public String generateServiceToken() {
        Instant now = Instant.now();

        return Jwts.builder()
                .issuer(serviceJwtProperties.getIssuer())
                .subject(serviceJwtProperties.getIssuer())
                .audience().add(serviceJwtProperties.getAudience()).and()
                .claim("scope", "RISK_EVALUATE")
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(serviceJwtProperties.getTtlSeconds())))
                .signWith(Keys.hmacShaKeyFor(serviceJwtProperties.getSecret().getBytes()), SignatureAlgorithm.HS256)
                .compact();

    }
}
