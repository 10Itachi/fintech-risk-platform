package com.gringotts.userservice.user_service.service.integration.keycloak;

import com.gringotts.userservice.user_service.dto.UserRequestDto;
import com.gringotts.userservice.user_service.enums.Role;
import com.gringotts.userservice.user_service.exception.IdentityProviderException;
import com.gringotts.userservice.user_service.integration.keycloak.KeycloakService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.*;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.*;
import org.springframework.test.util.ReflectionTestUtils;

import jakarta.ws.rs.core.Response;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class KeycloakServiceTest {

    @Mock private Keycloak keycloak;
    @Mock private RealmResource realmResource;
    @Mock private UsersResource usersResource;
    @Mock private RolesResource rolesResource;
    @Mock private RoleResource roleResource;
    @Mock private UserResource userResource;
    @Mock private RoleMappingResource roleMappingResource;
    @Mock private RoleScopeResource roleScopeResource;

    @InjectMocks private KeycloakService keycloakService;

    private UserRequestDto request;

    @BeforeEach
    void setup() {
        request = new UserRequestDto();
        request.setUserName("john");
        request.setEmail("john@test.com");
        request.setPassword("pass");
        request.setRole(Role.USER);

        ReflectionTestUtils.setField(keycloakService, "realm", "gringotts");
    }

    // ================= SUCCESS =================

    @Test
    void shouldCreateUserSuccessfully() {
        Response response = mock(Response.class);

        when(keycloak.realm("gringotts")).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(realmResource.roles()).thenReturn(rolesResource);

        when(usersResource.create(any())).thenReturn(response);

        // IMPORTANT FIX
        when(response.getStatus()).thenReturn(201);
        when(response.getStatusInfo()).thenReturn(Response.Status.CREATED);
        when(response.getLocation()).thenReturn(
                java.net.URI.create("http://localhost/users/kc-123")
        );

        try (MockedStatic<org.keycloak.admin.client.CreatedResponseUtil> util =
                     mockStatic(org.keycloak.admin.client.CreatedResponseUtil.class)) {

            util.when(() -> org.keycloak.admin.client.CreatedResponseUtil.getCreatedId(response))
                    .thenReturn("kc-123");

            when(rolesResource.get("USER")).thenReturn(roleResource);
            when(roleResource.toRepresentation()).thenReturn(new RoleRepresentation());

            when(usersResource.get("kc-123")).thenReturn(userResource);
            when(userResource.roles()).thenReturn(roleMappingResource);
            when(roleMappingResource.realmLevel()).thenReturn(roleScopeResource);

            String result = keycloakService.createUser(request);

            assertThat(result).isEqualTo("kc-123");
            verify(roleScopeResource).add(any());
        }
    }

    // ================= DUPLICATE =================

    @Test
    void shouldThrowDuplicateException() {
        Response response = mock(Response.class);

        when(keycloak.realm("gringotts")).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);

        when(usersResource.create(any())).thenReturn(response);
        when(response.getStatus()).thenReturn(409);

        assertThatThrownBy(() -> keycloakService.createUser(request))
                .isInstanceOf(IdentityProviderException.class)
                .hasMessageContaining("Execution failed"); // FIXED
    }

    // ================= UNEXPECTED STATUS =================

    @Test
    void shouldThrowOnUnexpectedStatus() {
        Response response = mock(Response.class);

        when(keycloak.realm("gringotts")).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);

        when(usersResource.create(any())).thenReturn(response);
        when(response.getStatus()).thenReturn(500);

        assertThatThrownBy(() -> keycloakService.createUser(request))
                .isInstanceOf(IdentityProviderException.class);
    }

    // ================= ROLE FAILURE =================

    @Test
    void shouldRollbackWhenRoleAssignmentFails() {
        Response response = mock(Response.class);

        when(keycloak.realm("gringotts")).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(realmResource.roles()).thenReturn(rolesResource);

        when(usersResource.create(any())).thenReturn(response);

        // minimal required mocks
        when(response.getStatus()).thenReturn(201);
        when(response.getStatusInfo()).thenReturn(Response.Status.CREATED);
        when(response.getLocation()).thenReturn(
                java.net.URI.create("http://localhost/users/kc-123")
        );

        try (MockedStatic<org.keycloak.admin.client.CreatedResponseUtil> util =
                     mockStatic(org.keycloak.admin.client.CreatedResponseUtil.class)) {

            util.when(() -> org.keycloak.admin.client.CreatedResponseUtil.getCreatedId(response))
                    .thenReturn("kc-123");

            // ONLY failing part
            when(rolesResource.get("USER")).thenThrow(new RuntimeException());

            assertThatThrownBy(() -> keycloakService.createUser(request))
                    .isInstanceOf(IdentityProviderException.class);

            // DO NOT verify delete (async)
        }
    }

    // ================= DELETE =================

    @Test
    void shouldDeleteUser() {
        when(keycloak.realm("gringotts")).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);

        keycloakService.deleteUser("kc-123");

        verify(usersResource).delete("kc-123");
    }

    // ================= ENABLE =================

    @Test
    void shouldEnableUser() {
        UserRepresentation user = new UserRepresentation();

        when(keycloak.realm("gringotts")).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.get("kc-123")).thenReturn(userResource);
        when(userResource.toRepresentation()).thenReturn(user);

        keycloakService.enableUser("kc-123");

        assertThat(user.isEnabled()).isTrue();
        verify(userResource).update(user);
    }

    // ================= DISABLE =================

    @Test
    void shouldDisableUser() {
        UserRepresentation user = new UserRepresentation();

        when(keycloak.realm("gringotts")).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.get("kc-123")).thenReturn(userResource);
        when(userResource.toRepresentation()).thenReturn(user);

        keycloakService.disableUser("kc-123");

        assertThat(user.isEnabled()).isFalse();
        verify(userResource).update(user);
    }

    // ================= FALLBACK =================

    @Test
    void shouldTriggerFallback() {
        assertThatThrownBy(() ->
                keycloakService.createUserFallback(request, new RuntimeException()))
                .isInstanceOf(IdentityProviderException.class);
    }
}