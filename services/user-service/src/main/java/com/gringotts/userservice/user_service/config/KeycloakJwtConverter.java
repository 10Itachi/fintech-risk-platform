package com.gringotts.userservice.user_service.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import java.util.*;

/**
 * JWT ROLE TRANSLATOR
 * -------------------
 * This component bridges the gap between Keycloak's token structure and Spring Security.
 * It extracts the 'realm_access.roles' JSON node from the JWT and converts them into
 * 'SimpleGrantedAuthority' objects with the mandatory 'ROLE_' prefix.
 * This allows @PreAuthorize("hasRole('...')") to work across the application.
 */
@Component
public class KeycloakJwtConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {

        Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();

        Map<String, Object> realmAccess = jwt.getClaim("realm_access");

        if (realmAccess != null) {

            Object rolesObj = realmAccess.get("roles");

            if (rolesObj instanceof Collection<?> roles) {
                authorities.addAll(
                        roles.stream()
                                .map(Object::toString)
                                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                                .toList()
                );
            }
        }

        return new JwtAuthenticationToken(jwt, authorities);
    }
}