package com.gringotts.transactionobservability.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

@Component
public class JwtAuthConverter implements Converter<Jwt, AbstractAuthenticationToken> {


    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        Collection<SimpleGrantedAuthority> authorities = extractRoles(jwt);
        return new JwtAuthenticationToken(jwt, authorities);
    }

    private Collection<SimpleGrantedAuthority> extractRoles(Jwt jwt) {
        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        if(realmAccess == null || !realmAccess.containsKey("roles")) {
            return Collections.emptyList();
        }
        Object objRoles = realmAccess.get("roles");
        if(objRoles instanceof Collection<?> roles) {
            return roles.stream()
                    .map(role->"ROLE_"+role.toString())
                    .map(SimpleGrantedAuthority::new)
                    .toList();
        }
        return Collections.emptyList();
    }

}
