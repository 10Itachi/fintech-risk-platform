package com.gringotts.risk.risk_decision_service.infrastructure.security;

import org.springframework.security.oauth2.core.*;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.*;

public class CustomJwtValidator implements OAuth2TokenValidator<Jwt> {

    // Allowed service clients
    private static final Set<String> ALLOWED_SERVICE_CLIENTS = Set.of(
            "transaction-service"
    );

    // User login client
    private static final Set<String> USER_CLIENTS = Set.of(
            "api-gateway",
            "gringotts-frontend"
    );

    // Expected audience
    private static final String EXPECTED_AUDIENCE = "risk-decision-service";

    // Required service role
    private static final String REQUIRED_SERVICE_ROLE = "RISKCALLER";

    @Override
    public OAuth2TokenValidatorResult validate(Jwt jwt) {

        List<OAuth2Error> errors = new ArrayList<>();

        // 1️⃣ Audience validation
        List<String> audiences = jwt.getAudience();
        if (audiences == null || !audiences.contains(EXPECTED_AUDIENCE)) {
            errors.add(new OAuth2Error(
                    "invalid_audience",
                    "Token not meant for this service",
                    null
            ));
        }

        // 2️⃣ Extract client (who requested token)
        String clientId = jwt.getClaimAsString("azp");

        if (clientId == null || clientId.isBlank()) {
            errors.add(new OAuth2Error(
                    "invalid_token",
                    "Missing client identifier (azp)",
                    null
            ));
            return OAuth2TokenValidatorResult.failure(errors);
        }

        // 3️⃣ USER TOKEN validation (api-gateway)
        if (USER_CLIENTS.contains(clientId)) {

            String subject = jwt.getSubject();
            if (subject == null || subject.isBlank()) {
                errors.add(new OAuth2Error(
                        "invalid_user",
                        "Invalid user token (missing subject)",
                        null
                ));
            }

            // NOTE:
            // User roles (ADMIN/USER) are validated in Spring Security config
        }

        // 4️⃣ SERVICE TOKEN validation (transaction-service)
        else if (ALLOWED_SERVICE_CLIENTS.contains(clientId)) {

            Map<String, Object> resourceAccess = jwt.getClaim("resource_access");

            if (resourceAccess == null || !resourceAccess.containsKey(EXPECTED_AUDIENCE)) {
                errors.add(new OAuth2Error(
                        "invalid_role",
                        "No access defined for risk-decision-service",
                        null
                ));
            } else {

                Object serviceObj = resourceAccess.get(EXPECTED_AUDIENCE);

                if (!(serviceObj instanceof Map)) {
                    errors.add(new OAuth2Error(
                            "invalid_role",
                            "Malformed resource_access structure",
                            null
                    ));
                } else {
                    Map<String, Object> service = (Map<String, Object>) serviceObj;

                    Object rolesObj = service.get("roles");

                    if (!(rolesObj instanceof List)) {
                        errors.add(new OAuth2Error(
                                "invalid_role",
                                "Roles missing or invalid",
                                null
                        ));
                    } else {
                        List<String> roles = (List<String>) rolesObj;

                        if (!roles.contains(REQUIRED_SERVICE_ROLE)) {
                            errors.add(new OAuth2Error(
                                    "invalid_role",
                                    "Missing required role: " + REQUIRED_SERVICE_ROLE,
                                    null
                            ));
                        }
                    }
                }
            }
        }

        // 5️⃣ Unknown client
        else {
            errors.add(new OAuth2Error(
                    "invalid_client",
                    "Unauthorized client: " + clientId,
                    null
            ));
        }

        return errors.isEmpty()
                ? OAuth2TokenValidatorResult.success()
                : OAuth2TokenValidatorResult.failure(errors);
    }
}

/*Default validation:
✔ signature
✔ expiry
✔ issuer

Custom validation:
✔ audience
✔ client (service-to-service security)
✔ token type*/