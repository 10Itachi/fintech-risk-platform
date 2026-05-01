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
import java.util.UUID;
import java.util.concurrent.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class KeycloakService implements IdentityProviderService {

    private final Keycloak keycloak;

    @Value("${keycloak.realm}")
    private String realm;

    private final ExecutorService executor = Executors.newFixedThreadPool(5);

    // ================= CREATE USER =================

    @Override
    @Retry(name = "keycloakRetry")
    @CircuitBreaker(name = "keycloakCB", fallbackMethod = "createUserFallback")
    public String createUser(UserRequestDto dto) {

        String traceId = UUID.randomUUID().toString();
        MDC.put("traceId", traceId);

        log.info("Creating user in Keycloak username={}", dto.getUserName());

        try {
            return executeWithTimeout(() -> doCreateUser(dto));
        } finally {
            MDC.clear();
        }
    }

    private String doCreateUser(UserRequestDto dto) {

        UsersResource usersResource = keycloak.realm(realm).users();
        UserRepresentation user = buildUserRepresentation(dto);

        try (Response response = usersResource.create(user)) {

            int status = response.getStatus();

            if (status == 201) {

                String userId = CreatedResponseUtil.getCreatedId(response);

                try {
                    assignRole(userId, dto.getRole());
                } catch (Exception roleEx) {

                    log.error("Role assignment failed, rolling back userId={}", userId, roleEx);
                    safeDelete(userId);

                    throw new IdentityProviderException("Role assignment failed");
                }

                log.info("User created successfully userId={}", userId);
                return userId;

            } else if (status == 409) {

                throw new IdentityProviderException("User already exists");

            } else {
                throw new IdentityProviderException("Unexpected response status=" + status);
            }

        } catch (Exception ex) {
            throw new IdentityProviderException("Keycloak create failed", ex);
        }
    }

    // ================= TIMEOUT WRAPPER =================

    //<T> generic object , T Generic return type
    private <T> T executeWithTimeout(Callable<T> task) {

        Future<T> future = executor.submit(task);
        //executor.submit(task): This takes your lambda (the instructions to create a user) and hands it to a separate thread.
        // It returns a Future, which is like a "claim ticket" for a result that isn't ready yet.

        try {
            return future.get(3, TimeUnit.SECONDS); // 🔥 timeout

        } catch (TimeoutException ex) {
            future.cancel(true);
            log.error("Keycloak call timed out");
            throw new IdentityProviderException("Keycloak timeout");

        } catch (Exception ex) {
            throw new IdentityProviderException("Execution failed", ex);
        }
    }

    // ================= FALLBACK =================

    public String createUserFallback(UserRequestDto dto, Throwable ex) {

        log.error("Fallback triggered for Keycloak createUser username={}", dto.getUserName(), ex);

        throw new IdentityProviderException("Identity provider unavailable, please try later");
    }

    // ================= DELETE =================

    @Override
    @Retry(name = "keycloakRetry")
    @CircuitBreaker(name = "keycloakCB")
    public void deleteUser(String userId) {
        safeDelete(userId);
    }

    private void safeDelete(String userId) {

        try {
            executeWithTimeout(() -> {
                keycloak.realm(realm).users().delete(userId);
                log.info("Deleted user userId={}", userId);
                return null;
            });

        } catch (Exception ex) {
            log.error("Failed to delete user userId={}", userId, ex);
            throw new IdentityProviderException("Delete failed", ex);
        }
    }

    // ================= ROLE =================

    private void assignRole(String userId, Enum<?> roleEnum) {

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

        log.info("Assigned role {} to user {}", roleEnum.name(), userId);
    }

    // ================= STATUS =================

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
            executeWithTimeout(() -> {
                UserRepresentation user = keycloak.realm(realm)
                        .users()
                        .get(userId)
                        .toRepresentation();

                user.setEnabled(enabled);

                keycloak.realm(realm)
                        .users()
                        .get(userId)
                        .update(user);

                return null;
            });

        } catch (Exception ex) {
            throw new IdentityProviderException("Update user status failed", ex);
        }
    }

    // ================= BUILD USER =================

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