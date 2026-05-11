package com.gringotts.userservice.user_service.integration.keycloak;

import com.gringotts.userservice.user_service.dto.UserRequestDto;
import com.gringotts.userservice.user_service.exception.IdentityProviderException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.*;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class KeycloakService implements IdentityProviderService {

    private final Keycloak keycloak;

    @Value("${keycloak.realm}")
    private String realm;

    @Override
    @Retry(name = "keycloakRetry")
    @CircuitBreaker(name = "keycloakCB")
    public String createUser(UserRequestDto dto) {

        String traceId = UUID.randomUUID().toString();
        MDC.put("traceId", traceId);

        log.info("Creating user in Keycloak username={}", dto.getUserName());

        try {
            return doCreateUser(dto);
        } catch (IdentityProviderException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IdentityProviderException("Keycloak communication failed", ex);
        } finally {
            MDC.clear();
        }
    }

    private String doCreateUser(UserRequestDto dto) {

        UsersResource users = keycloak.realm(realm).users();
        UserRepresentation user = buildUserRepresentation(dto);

        Response response = null;

        try {
            response = users.create(user);
            int status = response.getStatus();

            if (status == 201) {
                String userId = CreatedResponseUtil.getCreatedId(response);

                assignRole(userId, dto.getRole());

                log.info("User created in Keycloak userId={}", userId);
                return userId;
            }

            if (status == 409) {

                log.info("User already exists in Keycloak username={}", dto.getUserName());

                String existingUserId = findUserIdByUsername(dto.getUserName());

                if (existingUserId == null) {
                    throw new IdentityProviderException("User exists but unable to fetch userId");
                }

                return existingUserId;
            }

            throw new IdentityProviderException("Unexpected Keycloak response: " + status);

        } catch (IdentityProviderException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IdentityProviderException("Keycloak create failed", ex);
        } finally {
            if (response != null) response.close();
        }
    }

    private void assignRole(String userId, Enum<?> roleEnum) {
        try {
            RoleRepresentation role = keycloak.realm(realm)
                    .roles()
                    .get(roleEnum.name())
                    .toRepresentation();

            keycloak.realm(realm)
                    .users()
                    .get(userId)
                    .roles()
                    .realmLevel()
                    .add(Collections.singletonList(role));

        } catch (Exception ex) {
            log.error("Role assignment failed userId={}", userId, ex);
            throw new IdentityProviderException("Role assignment failed", ex);
        }
    }

    private String findUserIdByUsername(String username) {
        try {
            List<UserRepresentation> users = keycloak.realm(realm)
                    .users()
                    .search(username, true);

            if (users == null || users.isEmpty()) return null;

            return users.get(0).getId();

        } catch (Exception ex) {
            throw new IdentityProviderException("Failed to fetch existing user", ex);
        }
    }

    @Override
    @Retry(name = "keycloakRetry")
    @CircuitBreaker(name = "keycloakCB")
    public void deleteUser(String userId) {
        try {
            keycloak.realm(realm).users().delete(userId);
            log.info("Deleted Keycloak user userId={}", userId);
        } catch (Exception ex) {
            throw new IdentityProviderException("Delete user failed", ex);
        }
    }

    @Override
    public void disableUser(String userId) {
        updateUserStatus(userId, false);
    }

    @Override
    public void enableUser(String userId) {
        updateUserStatus(userId, true);
    }

    private void updateUserStatus(String userId, boolean enabled) {
        try {
            UserRepresentation user = keycloak.realm(realm)
                    .users()
                    .get(userId)
                    .toRepresentation();

            user.setEnabled(enabled);

            keycloak.realm(realm)
                    .users()
                    .get(userId)
                    .update(user);

        } catch (Exception ex) {
            throw new IdentityProviderException("Update user status failed", ex);
        }
    }

    private UserRepresentation buildUserRepresentation(UserRequestDto dto) {

        UserRepresentation user = new UserRepresentation();
        user.setUsername(dto.getUserName());
        user.setEmail(dto.getEmail());
        user.setEnabled(true);
        user.setEmailVerified(true);

        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(dto.getPassword());
        credential.setTemporary(false);

        user.setCredentials(Collections.singletonList(credential));

        return user;
    }
}