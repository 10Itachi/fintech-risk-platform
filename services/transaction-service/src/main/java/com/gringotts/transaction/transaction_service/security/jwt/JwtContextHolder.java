package com.gringotts.transaction.transaction_service.security.jwt;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class JwtContextHolder {

    public JwtContextHolder() {
    }

    public static Long getUserId() {
        Authentication authentication=
                SecurityContextHolder.getContext().getAuthentication();
        if(authentication==null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("No Authentication object found");
        }

        Object principal = authentication.getPrincipal();

        if(!(principal instanceof CustomerUserDetails)){
            throw new IllegalStateException("Principal is not of type CustomerUserDetails");
        }

        return ((CustomerUserDetails) principal).getUserId();
    }
}
