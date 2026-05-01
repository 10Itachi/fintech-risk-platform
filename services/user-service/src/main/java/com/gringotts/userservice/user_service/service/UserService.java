package com.gringotts.userservice.user_service.service;

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
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;


/**
 * CORE USER SERVICE (Orchestration Layer)
 * ---------------------------------------
 * Orchestrates user lifecycle events across the local MySql DB and Keycloak IDP.
 * Implements "Compensating Transactions" (Rollbacks) to ensure data consistency Saga pattern consistency plus roll back
 * between systems during failures. Includes integrated observability via UserMetrics
 * to monitor registration health and failure rates in real-time.
 */
@Service
@Slf4j
public class UserService {

    private final IdentityProviderService identityProviderService;
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final UserMetrics userMetrics;
    private final CacheManager cacheManager;
    public UserService(IdentityProviderService identityProviderService, UserRepository userRepository, UserMapper userMapper, UserMetrics userMetrics, CacheManager cacheManager) {
        this.identityProviderService = identityProviderService;
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.userMetrics = userMetrics;
        this.cacheManager = cacheManager;
    }


    @Transactional
    public UserResponseDto createUser(UserRequestDto request) {

        log.info("User creation started username={} email={}",
                request.getUserName(), request.getEmail());

        String keycloakUserId = null;

        try {
            // 1. Create user in Keycloak
            keycloakUserId = identityProviderService.createUser(request);
            log.info("Keycloak user created kcUserId={}", keycloakUserId);

            // 2. Map to entity
            User user = userMapper.toEntity(request, keycloakUserId);
            user.setIsActive(IsActive.ACTIVE);
            user.setCreatedAt(LocalDateTime.now());

            // 3. Save (DB handles uniqueness)
            User savedUser = userRepository.save(user);

            log.info("User created successfully. DB ID: {}, Keycloak ID: {}",
                    savedUser.getUserId(), keycloakUserId);

            userMetrics.incrementSuccess();

            return userMapper.toDto(savedUser);

        } catch (DataAccessException ex) {

            log.error("DB constraint violation or DB failure. Rolling back Keycloak user: {}",
                    keycloakUserId, ex);

            userMetrics.incrementFailure();
            rollbackKeycloakUser(keycloakUserId);

            //  IMPORTANT: detect duplicate properly
            if (ex.getMessage().contains("uk_users_username")) {
                throw new UserAlreadyExistsException("Username already exists");
            } else if (ex.getMessage().contains("uk_users_email")) {
                throw new UserAlreadyExistsException("Email already exists");
            } else if (ex.getMessage().contains("uk_users_phone")) {
                throw new UserAlreadyExistsException("Phone number already exists");
            }

            throw new UserServiceException("User creation failed due to database error");

        } catch (RuntimeException ex) {

            log.error("User creation failed for username: {}", request.getUserName(), ex);

            userMetrics.incrementFailure();
            rollbackKeycloakUser(keycloakUserId);

            throw new UserServiceException("User creation failed");
        }
    }

    // 🔥 Compensation logic isolated
    private void rollbackKeycloakUser(String keycloakUserId) {

        if (keycloakUserId == null) return;

        for (int attempt = 1; attempt <= 3; attempt++) {
            try {
                log.warn("Rollback attempt {} for kcUserId={}", attempt, keycloakUserId);

                identityProviderService.deleteUser(keycloakUserId);

                log.info("Rollback successful kcUserId={}", keycloakUserId);
                return;

            } catch (Exception ex) {
                log.error("Rollback attempt {} failed kcUserId={}", attempt, keycloakUserId, ex);

                try {
                    Thread.sleep(500L * attempt); // basic backoff
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }

        log.error("CRITICAL: Failed to rollback Keycloak user kcUserId={}", keycloakUserId);
    }

    @Cacheable(value = "userByUsername", key = "#username")
    public UserResponseDto  getUserByUsername(String username) {
        log.info("DB HIT → fetching username={}", username);
        User user = userRepository
                .findByUserNameAndIsActive(username, IsActive.ACTIVE)
                .orElseThrow(() -> new UserNotFound("User not found"));

        return userMapper.toDto(user);
    }

    public List<UserResponseDto> getUsersAfterId(Long lastId, int size) {

        return userRepository
                .findByUserIdGreaterThanOrderByUserIdAsc(lastId, Pageable.ofSize(size))
                .stream()
                .map(userMapper::toDto)
                .toList();
    }

    @Cacheable(value = "userById",key = "#userId")
    public UserResponseDto getUserById(Long userId) {
        log.info("DB HIT → fetching userId={}", userId);
        User user = userRepository
                .findByUserIdAndIsActive(userId, IsActive.ACTIVE)
                .orElseThrow(() -> new UserNotFound("User not found"));

        return userMapper.toDto(user);
    }

    @Transactional
    public void deactivateUser(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFound("User not found"));

        if (user.getIsActive() == IsActive.INACTIVE) return;

        user.setIsActive(IsActive.INACTIVE);

        identityProviderService.disableUser(user.getKeycloakUserId());
        // 🔥 Manual eviction (clean & reliable)
        cacheManager.getCache("userById").evict(userId);
        cacheManager.getCache("userByUsername").evict(user.getUserName());
    }

    @Transactional
    public void reactivateUser(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFound("User not found"));

        if (user.getIsActive() == IsActive.ACTIVE) return;

        user.setIsActive(IsActive.ACTIVE);

        identityProviderService.enableUser(user.getKeycloakUserId());
        cacheManager.getCache("userById").evict(userId);
        cacheManager.getCache("userByUsername").evict(user.getUserName());
    }
}