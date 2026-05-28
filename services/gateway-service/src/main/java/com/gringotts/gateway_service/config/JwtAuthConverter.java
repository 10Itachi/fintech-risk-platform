    package com.gringotts.gateway_service.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class JwtAuthConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        Collection<SimpleGrantedAuthority> authorities = extractRoles(jwt);
        return new JwtAuthenticationToken(
                jwt,
                authorities,
                jwt.getSubject()
        );
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