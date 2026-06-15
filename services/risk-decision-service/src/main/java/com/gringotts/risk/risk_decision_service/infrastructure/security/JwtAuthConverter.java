package com.gringotts.risk.risk_decision_service.infrastructure.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class JwtAuthConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private static final String SERVICE_ID = "risk-decision-service";

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        Collection<SimpleGrantedAuthority> authorities = extractRoles(jwt);
        return new JwtAuthenticationToken(jwt, authorities);
    }

    private Collection<SimpleGrantedAuthority> extractRoles(Jwt jwt) {

        Set<String> roles = new HashSet<>();

        // 1️⃣ Extract REALM roles (USER / ADMIN)
        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        if (realmAccess != null && realmAccess.get("roles") instanceof Collection<?> realmRoles) {
            realmRoles.forEach(role -> roles.add(role.toString()));
        }

        // 2️⃣ Extract CLIENT roles (RISKCALLER)
        Map<String, Object> resourceAccess = jwt.getClaim("resource_access");

        if (resourceAccess != null && resourceAccess.containsKey(SERVICE_ID)) {

            Object serviceObj = resourceAccess.get(SERVICE_ID);

            if (serviceObj instanceof Map<?, ?> serviceMap) {

                Object rolesObj = serviceMap.get("roles");

                if (rolesObj instanceof Collection<?> serviceRoles) {
                    serviceRoles.forEach(role -> roles.add(role.toString()));
                }
            }
        }

        // 3️⃣ Convert to Spring Authorities
        return roles.stream()
                .map(role -> "ROLE_" + role)
                .map(SimpleGrantedAuthority::new)
                .toList();
    }
}