package com.gringotts.userservice.user_service.scheduler;

import com.gringotts.userservice.user_service.entity.User;
import com.gringotts.userservice.user_service.enums.IsActive;
import com.gringotts.userservice.user_service.enums.Role;
import com.gringotts.userservice.user_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class KeycloakToDbReconciliationScheduler {

    private final Keycloak keycloak;
    private final UserRepository userRepository;

    // inject from yml
    private final String realm = System.getenv("USER_KEYCLOAK_REALM");

    private static final int PAGE_SIZE = 50;

    @Scheduled(fixedDelayString = "${scheduler.reconciliation.delay}",
    initialDelayString = "${scheduler.reconciliation.initial-delay}")
    public void syncKeycloakToDb() {

        log.info("Starting Keycloak → DB reconciliation");

        UsersResource usersResource = keycloak.realm(realm).users();

        int first = 0;

        while (true) {

            List<UserRepresentation> kcUsers = usersResource.list(first, PAGE_SIZE);

            if (kcUsers == null || kcUsers.isEmpty()) break;

            for (UserRepresentation kcUser : kcUsers) {

                try {
                    processUser(kcUser);
                } catch (Exception ex) {
                    log.error("Failed syncing user {}", kcUser.getUsername(), ex);
                }

                // small throttle
                sleep(5);
            }

            first += PAGE_SIZE;
        }

        log.info("Completed Keycloak → DB reconciliation");
    }

    private void processUser(UserRepresentation kcUser) {

        String username = kcUser.getUsername();

        userRepository.findByUserNameAndIsActive(username, IsActive.ACTIVE)
                .ifPresentOrElse(existing -> {

                    // ✔ fix mismatched kcId if needed
                    if (!existing.getKeycloakUserId().equals(kcUser.getId())) {
                        log.warn("Fixing KC ID mismatch for {}", username);
                        existing.setKeycloakUserId(kcUser.getId());
                        userRepository.save(existing);
                    }

                }, () -> {

                    // ✔ create missing DB user
                    log.warn("User missing in DB, creating {}", username);

                    User newUser = new User();
                    newUser.setUserName(username);
                    newUser.setEmail(kcUser.getEmail());
                    newUser.setPhoneNumber(extractPhone(kcUser)); // optional
                    newUser.setKeycloakUserId(kcUser.getId());
                    newUser.setIsActive(IsActive.ACTIVE);
                    newUser.setCreatedAt(LocalDateTime.now());

                    newUser.setRole(fetchRole(kcUser)); // simplified

                    userRepository.save(newUser);
                });
    }

    private String extractPhone(UserRepresentation kcUser) {
        if (kcUser.getAttributes() != null && kcUser.getAttributes().containsKey("phone")) {
            return kcUser.getAttributes().get("phone").get(0);
        }
        return null;
    }

    private Role fetchRole(UserRepresentation kcUser) {

        try {
            List<String> roles = keycloak.realm(realm)
                    .users()
                    .get(kcUser.getId())
                    .roles()
                    .realmLevel()
                    .listAll()
                    .stream()
                    .map(role -> role.getName())
                    .toList();

            if (roles.contains("ADMIN")) {
                return Role.ADMIN;
            }

            return Role.USER;

        } catch (Exception ex) {
            log.error("Failed to fetch roles for user {}", kcUser.getUsername(), ex);
            return Role.USER; // fallback
        }
    }

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ignored) {}
    }
}