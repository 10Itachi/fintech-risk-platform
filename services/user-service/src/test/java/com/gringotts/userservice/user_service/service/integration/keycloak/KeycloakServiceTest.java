package com.gringotts.userservice.user_service.service.integration.keycloak;

import com.gringotts.userservice.user_service.dto.UserRequestDto;
import com.gringotts.userservice.user_service.enums.Role;
import com.gringotts.userservice.user_service.exception.IdentityProviderException;
import com.gringotts.userservice.user_service.integration.keycloak.KeycloakService;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.*;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;


import java.util.List;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KeycloakServiceTest {

    @Mock private Keycloak keycloak;
    @Mock private RealmResource realmResource;
    @Mock private UsersResource usersResource;
    @Mock private Response response;

    @InjectMocks
    private KeycloakService keycloakService;

    private UserRequestDto dto;

    @BeforeEach
    void setup() {
        dto = new UserRequestDto();
        dto.setUserName("john");
        dto.setEmail("john@test.com");
        dto.setRole(Role.USER); // ✅ IMPORTANT FIX

        ReflectionTestUtils.setField(keycloakService, "realm", "test");

        when(keycloak.realm("test")).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
    }

    // ================= SUCCESS =================

    @Test
    void createUser_success() {

        when(usersResource.create(any())).thenReturn(response);
        when(response.getStatus()).thenReturn(201);

        try (MockedStatic<CreatedResponseUtil> util = mockStatic(CreatedResponseUtil.class)) {
            util.when(() -> CreatedResponseUtil.getCreatedId(response))
                    .thenReturn("kc-123");

            // ✅ MOCK ROLE CHAIN
            RolesResource rolesResource = mock(RolesResource.class);
            RoleResource roleResource = mock(RoleResource.class);
            RoleRepresentation roleRepresentation = new RoleRepresentation();

            when(realmResource.roles()).thenReturn(rolesResource);
            when(rolesResource.get("USER")).thenReturn(roleResource);
            when(roleResource.toRepresentation()).thenReturn(roleRepresentation);

            // ✅ MOCK USER ROLE ASSIGNMENT CHAIN
            UserResource userResource = mock(UserResource.class);
            RoleMappingResource roleMappingResource = mock(RoleMappingResource.class);
            RoleScopeResource roleScopeResource = mock(RoleScopeResource.class);

            when(realmResource.users().get("kc-123")).thenReturn(userResource);
            when(userResource.roles()).thenReturn(roleMappingResource);
            when(roleMappingResource.realmLevel()).thenReturn(roleScopeResource);

            String result = keycloakService.createUser(dto);

            assertThat(result).isEqualTo("kc-123");
        }
    }

    // ================= CONFLICT =================

    @Test
    void createUser_conflict() {

        when(usersResource.create(any())).thenReturn(response);
        when(response.getStatus()).thenReturn(409);

        UserRepresentation existingUser = new UserRepresentation();
        existingUser.setId("existing-id");

        when(usersResource.search("john", true)).thenReturn(List.of(existingUser));

        String result = keycloakService.createUser(dto);

        assertThat(result).isEqualTo("existing-id");
    }

    // ================= FAILURE =================

    @Test
    void createUser_failure() {

        when(usersResource.create(any())).thenReturn(response);
        when(response.getStatus()).thenReturn(500);

        assertThatThrownBy(() -> keycloakService.createUser(dto))
                .isInstanceOf(IdentityProviderException.class);
    }

    // ================= DELETE =================

    @Test
    void deleteUser_success() {
        keycloakService.deleteUser("kc-123");

        verify(keycloak.realm("test").users()).delete("kc-123");
    }

    @Test
    void deleteUser_failure() {

        UsersResource usersResourceMock = mock(UsersResource.class);

        when(keycloak.realm("test")).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResourceMock);

        doThrow(new RuntimeException())
                .when(usersResourceMock)
                .delete("kc-123");

        assertThatThrownBy(() -> keycloakService.deleteUser("kc-123"))
                .isInstanceOf(IdentityProviderException.class);
    }
}