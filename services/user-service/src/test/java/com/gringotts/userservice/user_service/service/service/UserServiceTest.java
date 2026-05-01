package com.gringotts.userservice.user_service.service.service;

import com.gringotts.userservice.user_service.dto.UserRequestDto;
import com.gringotts.userservice.user_service.dto.UserResponseDto;
import com.gringotts.userservice.user_service.entity.User;
import com.gringotts.userservice.user_service.enums.IsActive;
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
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.dao.DataAccessException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class UserServiceTest {

    @Mock private IdentityProviderService identityProviderService;
    @Mock private UserRepository userRepository;
    @Mock private UserMapper userMapper;
    @Mock private UserMetrics userMetrics;
    @Mock private CacheManager cacheManager;
    @Mock private Cache cache;

    @InjectMocks private UserService userService;

    private UserRequestDto request;
    private User user;
    private UserResponseDto responseDto;

    @BeforeEach
    void setup() {
        request = new UserRequestDto();
        request.setUserName("john");
        request.setEmail("john@test.com");
        request.setPhoneNumber("9999999999");

        user = new User();
        user.setUserId(1L);
        user.setUserName("john");
        user.setKeycloakUserId("kc-123");
        user.setIsActive(IsActive.ACTIVE);

        responseDto = new UserResponseDto(1L, "john", IsActive.ACTIVE);
    }

    // ========================= CREATE USER =========================

    @Test
    void shouldCreateUserSuccessfully() {
        when(identityProviderService.createUser(request)).thenReturn("kc-123");
        when(userMapper.toEntity(request, "kc-123")).thenReturn(user);
        when(userRepository.save(any())).thenReturn(user);
        when(userMapper.toDto(user)).thenReturn(responseDto);

        UserResponseDto result = userService.createUser(request);

        assertThat(result).isNotNull();
        assertThat(result.getUserName()).isEqualTo("john");

        verify(identityProviderService).createUser(request);
        verify(userRepository).save(any());
        verify(userMetrics).incrementSuccess();
    }

    @Test
    void shouldRollbackWhenDbFails() {
        when(identityProviderService.createUser(request)).thenReturn("kc-123");
        when(userMapper.toEntity(any(), any())).thenReturn(user);

        DataAccessException ex = mock(DataAccessException.class);
        when(ex.getMessage()).thenReturn("some-db-error"); // IMPORTANT

        when(userRepository.save(any())).thenThrow(ex);

        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(UserServiceException.class);

        verify(identityProviderService).deleteUser("kc-123");
        verify(userMetrics).incrementFailure();
    }

    @Test
    void shouldThrowDuplicateExceptionWhenUsernameExists() {
        when(identityProviderService.createUser(request)).thenReturn("kc-123");
        when(userMapper.toEntity(any(), any())).thenReturn(user);

        DataAccessException ex = mock(DataAccessException.class);
        when(ex.getMessage()).thenReturn("uk_users_username");
        when(userRepository.save(any())).thenThrow(ex);

        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("Username already exists");

        verify(identityProviderService).deleteUser("kc-123");
    }

    @Test
    void shouldRollbackOnRuntimeException() {
        when(identityProviderService.createUser(request))
                .thenThrow(new RuntimeException("Keycloak down"));

        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(UserServiceException.class);

        verify(userMetrics).incrementFailure();
        verify(identityProviderService, never()).deleteUser(any());
    }

    // ========================= READ =========================

    @Test
    void shouldReturnUserById() {
        when(userRepository.findByUserIdAndIsActive(1L, IsActive.ACTIVE))
                .thenReturn(Optional.of(user));
        when(userMapper.toDto(user)).thenReturn(responseDto);

        UserResponseDto result = userService.getUserById(1L);

        assertThat(result.getUserId()).isEqualTo(1L);
    }

    @Test
    void shouldThrowWhenUserNotFound() {
        when(userRepository.findByUserIdAndIsActive(1L, IsActive.ACTIVE))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(1L))
                .isInstanceOf(UserNotFound.class);
    }

    // ========================= STATE CHANGE =========================

    @Test
    void shouldDeactivateUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cacheManager.getCache("userById")).thenReturn(cache);
        when(cacheManager.getCache("userByUsername")).thenReturn(cache);

        userService.deactivateUser(1L);

        assertThat(user.getIsActive()).isEqualTo(IsActive.INACTIVE);

        verify(identityProviderService).disableUser("kc-123");
        verify(cache).evict(1L);
        verify(cache).evict("john");
    }

    @Test
    void shouldReactivateUser() {
        user.setIsActive(IsActive.INACTIVE);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cacheManager.getCache("userById")).thenReturn(cache);
        when(cacheManager.getCache("userByUsername")).thenReturn(cache);

        userService.reactivateUser(1L);

        assertThat(user.getIsActive()).isEqualTo(IsActive.ACTIVE);

        verify(identityProviderService).enableUser("kc-123");
        verify(cache).evict(1L);
        verify(cache).evict("john");
    }
}