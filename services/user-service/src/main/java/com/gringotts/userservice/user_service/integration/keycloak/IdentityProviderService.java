package com.gringotts.userservice.user_service.integration.keycloak;

import com.gringotts.userservice.user_service.dto.UserRequestDto;

/**
 * IDENTITY PROVIDER ABSTRACTION (Strategy Pattern)
 * -----------------------------------------------
 * PURPOSE:
 * This interface acts as a "Contract" for user management. It decouples our
 * business logic (User Service) from the specific security vendor (Keycloak).
 *
 * WHY THIS ARCHITECTURE?
 * 1. Vendor Agnostic: We can switch from Keycloak to Auth0 or AWS Cognito
 * later by simply creating a new implementation of this interface.
 * 2. Dependency Inversion: High-level business logic depends on this
 * abstraction, not the low-level Keycloak technical details.
 * 3. Maintainability: All OAuth2/Provider-specific logic is encapsulated
 * in the implementation, keeping the rest of the microservice clean.
 *
 * IMPLEMENTATION NOTE:
 * The current implementation (KeycloakServiceImpl) uses a Machine-to-Machine
 * (M2M) Client Credentials flow to securely manage users in the Keycloak realm.
 */
public interface IdentityProviderService {
    String createUser(UserRequestDto request);

    void deleteUser(String userId);

    void enableUser(String userId);

    void disableUser(String userId);
}
