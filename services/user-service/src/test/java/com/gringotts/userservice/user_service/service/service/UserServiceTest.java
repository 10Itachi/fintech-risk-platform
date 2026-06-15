package com.gringotts.userservice.user_service.service.service;

import com.gringotts.userservice.user_service.dto.UserRequestDto;
import com.gringotts.userservice.user_service.dto.UserResponseDto;
import com.gringotts.userservice.user_service.entity.User;
import com.gringotts.userservice.user_service.enums.IsActive;
import com.gringotts.userservice.user_service.exception.IdentityProviderException;
import com.gringotts.userservice.user_service.exception.UserAlreadyExistsException;
import com.gringotts.userservice.user_service.exception.UserNotFound;
import com.gringotts.userservice.user_service.exception.UserServiceException;
import com.gringotts.userservice.user_service.integration.keycloak.IdentityProviderService;
import com.gringotts.userservice.user_service.mapper.UserMapper;
import com.gringotts.userservice.user_service.repository.UserRepository;
import com.gringotts.userservice.user_service.service.UserMetrics;
import com.gringotts.userservice.user_service.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.dao.DataAccessException;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private IdentityProviderService identityProviderService;
    @Mock private UserRepository userRepository;
    @Mock private UserMapper userMapper;
    @Mock private UserMetrics userMetrics;
    @Mock private CacheManager cacheManager;
    @Mock private Cache cache;

    @InjectMocks
    private UserService userService;

    private UserRequestDto request;
    private User user;
    private UserResponseDto response;

    @BeforeEach
    void setup() {
        request = new UserRequestDto();
        request.setUserName("john");
        request.setEmail("john@test.com");

        user = new User();
        user.setUserId(UUID.fromString("00000000-0000-0000-0000-000000000001"));
        user.setUserName("john");
        user.setKeycloakUserId("kc-123");

        response = new UserResponseDto();
    }

    // ================= CREATE USER =================

    @Test
    void createUser_success() {

        when(identityProviderService.createUser(request)).thenReturn("kc-123");
        when(userMapper.toEntity(request, "kc-123")).thenReturn(user);
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toDto(user)).thenReturn(response);

        UserResponseDto result = userService.createUser(request);

        assertThat(result).isNotNull();

        verify(identityProviderService).createUser(request);
        verify(userRepository).save(user);
        verify(userMetrics).incrementSuccess();
    }

    @Test
    void createUser_keycloakFailure() {

        when(identityProviderService.createUser(request))
                .thenThrow(new IdentityProviderException("fail"));

        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(IdentityProviderException.class);

        verify(userRepository, never()).save(any());
        verify(userMetrics).incrementFailure();
    }

    @Test
    void createUser_dbFailure_shouldRollback() {

        when(identityProviderService.createUser(request)).thenReturn("kc-123");
        when(userMapper.toEntity(request, "kc-123")).thenReturn(user);
        when(userRepository.save(user))
                .thenThrow(new DataAccessException("db error") {});

        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(UserServiceException.class);

        verify(identityProviderService).deleteUser("kc-123");
        verify(userMetrics).incrementFailure();
    }

    @Test
    void createUser_duplicateUser() {

        when(identityProviderService.createUser(request)).thenReturn("kc-123");
        when(userMapper.toEntity(request, "kc-123")).thenReturn(user);

        // ✅ Updated to realistic DB error message
        when(userRepository.save(user))
                .thenThrow(new DataAccessException(
                        "duplicate key value violates unique constraint uk_users_email"
                ) {});

        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(UserAlreadyExistsException.class);

        verify(identityProviderService).deleteUser("kc-123");
    }

    // ================= READ =================

    @Test
    void getUserByKeycloakUserId_success() {

        when(userRepository.findByKeycloakUserIdAndIsActive("kc-123", IsActive.ACTIVE))
                .thenReturn(Optional.of(user));
        when(userMapper.toDto(user)).thenReturn(response);

        UserResponseDto result = userService.getUserByKeycloakId("kc-123");

        assertThat(result).isNotNull();
    }

    @Test
    void getUserByKeycloakUserId_notFound() {

        when(userRepository.findByKeycloakUserIdAndIsActive(any(), any()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserByKeycloakId("kc-123"))
                .isInstanceOf(UserNotFound.class);
    }

    // ================= STATUS =================

    @Test
    void deactivateUser_shouldDisableAndEvictCache() {

        user.setIsActive(IsActive.ACTIVE);

        when(userRepository.findByUserId(UUID.fromString("00000000-0000-0000-0000-000000000001"))).thenReturn(Optional.of(user));
        when(cacheManager.getCache("userById")).thenReturn(cache);
        when(cacheManager.getCache("userByUsername")).thenReturn(cache);

        userService.deactivateUser(UUID.fromString("00000000-0000-0000-0000-000000000001"));

        verify(identityProviderService).disableUser("kc-123");
        verify(cache).evict(UUID.fromString("00000000-0000-0000-0000-000000000001"));
        verify(cache).evict("john");
    }

    @Test
    void reactivateUser_shouldEnableAndEvictCache() {

        user.setIsActive(IsActive.INACTIVE);

        when(userRepository.findByUserId(UUID.fromString("00000000-0000-0000-0000-000000000001"))).thenReturn(Optional.of(user));
        when(cacheManager.getCache("userById")).thenReturn(cache);
        when(cacheManager.getCache("userByUsername")).thenReturn(cache);

        userService.reactivateUser(UUID.fromString("00000000-0000-0000-0000-000000000001"));

        verify(identityProviderService).enableUser("kc-123");
        verify(cache).evict(UUID.fromString("00000000-0000-0000-0000-000000000001"));
        verify(cache).evict("john");
    }
}