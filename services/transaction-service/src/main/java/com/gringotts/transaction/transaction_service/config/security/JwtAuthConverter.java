package com.gringotts.transaction.transaction_service.config.security;


import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.*;

// Converts a validated Jwt into a Spring Security Authentication object.
// Extracts roles from JWT claims (realm_access.roles) and maps them to ROLE_* authorities.
// Creates JwtAuthenticationToken with these authorities for authorization checks (@PreAuthorize).
// This converter is invoked during the security filter chain after JWT validation.

@Component
public class JwtAuthConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        Collection<SimpleGrantedAuthority> authorities = extractRoles(jwt);
        return new JwtAuthenticationToken(jwt, authorities);
    }

    private Collection<SimpleGrantedAuthority> extractRoles(Jwt jwt) {
        Map<String, Object> realmAccess = jwt.getClaim("realm_access");

        if(realmAccess == null || realmAccess.get("roles") == null) {
            return Collections.emptyList();
        }

        Object objRoles = realmAccess.get("roles");
        if(objRoles instanceof Collection<?> roles){
            return roles.stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toString()))
                    .toList(); // Simplified in modern Java
        }
        return Collections.emptyList();
    }
}
