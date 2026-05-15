package com.gringotts.transaction.transaction_service.infrastructure.keycloak;

import io.github.resilience4j.retry.annotation.Retry;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

import lombok.extern.slf4j.Slf4j;

import org.springframework.http.*;
import org.springframework.stereotype.Service;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.Map;

@Slf4j
@Service
public class ServiceTokenGenerator {

    private static final long TOKEN_REFRESH_BUFFER_SECONDS = 30;

    private final RestTemplate restTemplate;
    private final ServiceJwtProperties properties;
    private final MeterRegistry meterRegistry;

    private volatile String cachedToken;
    private volatile Instant expiryTime;

    public ServiceTokenGenerator(
            RestTemplate keycloakRestTemplate,
            ServiceJwtProperties properties,
            MeterRegistry meterRegistry
    ) {

        this.restTemplate = keycloakRestTemplate;
        this.properties = properties;
        this.meterRegistry = meterRegistry;
    }

    /*
     =========================================================
     GET TOKEN
     =========================================================

     Returns cached token if still valid.

     Otherwise fetches new token from Keycloak.
     */
    public synchronized String getToken() {

        if (cachedToken != null &&
                expiryTime != null &&
                Instant.now().isBefore(expiryTime)) {

            meterRegistry.counter(
                    "service.token.cache.hit"
            ).increment();

            return cachedToken;
        }

        meterRegistry.counter(
                "service.token.cache.miss"
        ).increment();

        return fetchNewToken();
    }

    /*
     =========================================================
     FETCH NEW TOKEN
     =========================================================

     Uses client_credentials flow to obtain
     machine-to-machine JWT token.
     */
    @Retry(name = "keycloakToken")
    public String fetchNewToken() {

        Timer.Sample timer =
                Timer.start(meterRegistry);

        try {

            log.info(
                    "Fetching new service token from Keycloak"
            );

            HttpHeaders headers = new HttpHeaders();

            headers.setContentType(
                    MediaType.APPLICATION_FORM_URLENCODED
            );

            MultiValueMap<String, String> body =
                    new LinkedMultiValueMap<>();

            body.add(
                    "grant_type",
                    "client_credentials"
            );

            body.add(
                    "client_id",
                    properties.getClientId()
            );

            body.add(
                    "client_secret",
                    properties.getClientSecret()
            );

            HttpEntity<MultiValueMap<String, String>> request =
                    new HttpEntity<>(body, headers);

            ResponseEntity<Map> response =
                    restTemplate.exchange(
                            properties.getTokenUrl(),
                            HttpMethod.POST,
                            request,
                            Map.class
                    );

            Map responseBody = response.getBody();

            if (responseBody == null) {

                throw new RuntimeException(
                        "Keycloak token response body is null"
                );
            }

            String accessToken =
                    (String) responseBody.get("access_token");

            Integer expiresIn =
                    (Integer) responseBody.get("expires_in");

            if (accessToken == null || expiresIn == null) {

                throw new RuntimeException(
                        "Invalid token response from Keycloak"
                );
            }

            this.cachedToken = accessToken;

            this.expiryTime =
                    Instant.now().plusSeconds(
                            expiresIn -
                                    TOKEN_REFRESH_BUFFER_SECONDS
                    );

            meterRegistry.counter(
                    "service.token.success"
            ).increment();

            log.info(
                    "Service token fetched successfully"
            );
            log.info(
                    "ACCESS TOKEN PREFIX={}",
                    accessToken.substring(0, 40)
            );
            return accessToken;

        } catch (Exception ex) {

            meterRegistry.counter(
                    "service.token.failure"
            ).increment();

            log.error(
                    "Failed to fetch service token from Keycloak",
                    ex
            );

            throw ex;

        } finally {

            timer.stop(
                    meterRegistry.timer(
                            "service.token.latency"
                    )
            );
        }
    }
}