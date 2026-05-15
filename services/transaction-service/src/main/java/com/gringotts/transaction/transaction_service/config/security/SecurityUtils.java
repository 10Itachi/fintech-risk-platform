package com.gringotts.transaction.transaction_service.config.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.UUID;

/*
 =========================================================
 SECURITY UTILITIES
 =========================================================

 Utility class for extracting authenticated user details
 from Spring SecurityContext.

 JWT is already validated by Spring Security before
 reaching application layer.

 Used for:
 - userId extraction
 - username extraction
 - email extraction
 - downstream event enrichment
*/
public final class SecurityUtils {

    private SecurityUtils() {
    }

    /*
     =====================================================
     GET AUTHENTICATED JWT
     =====================================================
     */
    private static Jwt getJwt() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null ||
                !(authentication.getPrincipal() instanceof Jwt jwt)) {

            throw new IllegalStateException(
                    "Authenticated JWT not found"
            );
        }

        return jwt;
    }

    /*
     =====================================================
     USER ID
     =====================================================
     */
    public static UUID getUserId() {

        return UUID.fromString(
                getJwt().getSubject()
        );
    }

    /*
     =====================================================
     USERNAME
     =====================================================
     */
    public static String getUsername() {

        return getJwt().getClaimAsString(
                "preferred_username"
        );
    }

    /*
     =====================================================
     EMAIL
     =====================================================
     */
    public static String getEmail() {

        return getJwt().getClaimAsString(
                "email"
        );
    }

    /*
     =====================================================
     PHONE NUMBER
     =====================================================
     */
    public static String getPhoneNumber() {

        return getJwt().getClaimAsString(
                "phone_number"
        );
    }
}
