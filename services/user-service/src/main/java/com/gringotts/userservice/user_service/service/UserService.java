package com.gringotts.userservice.user_service.service;

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

    public UserService(IdentityProviderService identityProviderService,
                       UserRepository userRepository,
                       UserMapper userMapper,
                       UserMetrics userMetrics,
                       CacheManager cacheManager) {
        this.identityProviderService = identityProviderService;
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.userMetrics = userMetrics;
        this.cacheManager = cacheManager;
    }

    // ================= CREATE USER =================

    /*@Transactional is placed at UserService level because DB operations must rollback atomically.
    Keycloak is an external HTTP system and cannot participate in Spring DB transactions.*/
    @Transactional
    public UserResponseDto createUser(UserRequestDto request) {

        log.info("User creation started username={} email={}",
                request.getUserName(), request.getEmail());

        String keycloakUserId = null;

        try {
            // Create (or fetch) user in Keycloak (IDEMPOTENT)
            keycloakUserId = identityProviderService.createUser(request);

            log.info("Keycloak user resolved kcUserId={}", keycloakUserId);

            // Save in DB (SOURCE OF TRUTH)
            User user = userMapper.toEntity(request, keycloakUserId);
            user.setIsActive(IsActive.ACTIVE);
            user.setCreatedAt(LocalDateTime.now());

            User savedUser = userRepository.save(user);

            log.info("User persisted DB ID={} KC ID={}",
                    savedUser.getUserId(), keycloakUserId);

            userMetrics.incrementSuccess();
            return userMapper.toDto(savedUser);

        }

        // ================= KEYCLOAK FAILURE =================
        catch (IdentityProviderException ex) {

            log.error("Keycloak failure username={}", request.getUserName(), ex);

            userMetrics.incrementFailure();

            // rollback only if user was created
            if (keycloakUserId != null) {
                safeRollback(keycloakUserId);
            }
            throw ex;
        }

        // ================= DB FAILURE =================
        catch (DataAccessException ex) {

            log.error("DB failure. Rolling back Keycloak user={}", keycloakUserId, ex);

            userMetrics.incrementFailure();
            safeRollback(keycloakUserId);

            // rely on DB constraint instead of string parsing ideally
            if (isDuplicateError(ex)) {
                throw new UserAlreadyExistsException("User with same email/phone/username already exists");
            }
            throw new UserServiceException("Database error during user creation");
        }

        // ================= UNKNOWN FAILURE =================
        catch (Exception ex) {

            log.error("Unexpected failure username={}", request.getUserName(), ex);

            userMetrics.incrementFailure();
            safeRollback(keycloakUserId);

            throw new UserServiceException("Unexpected error during user creation");
        }
    }

    // ================= ROLLBACK =================
    private void safeRollback(String keycloakUserId) {

        if (keycloakUserId == null) return;
        try {
            identityProviderService.deleteUser(keycloakUserId);
            log.warn("Rollback successful kcUserId={}", keycloakUserId);

        } catch (Exception ex) {
            log.error("CRITICAL: Rollback failed kcUserId={}", keycloakUserId, ex);
        }
    }

    // ================= DUPLICATE CHECK =================
    private boolean isDuplicateError(DataAccessException ex) {

        // check BOTH root cause and main exception
        String message = null;

        if (ex.getRootCause() != null && ex.getRootCause().getMessage() != null) {
            message = ex.getRootCause().getMessage().toLowerCase();
        } else if (ex.getMessage() != null) {
            message = ex.getMessage().toLowerCase();
        }

        if (message == null) return false;

        return message.contains("duplicate") ||
                message.contains("email") ||
                message.contains("phone") ||
                message.contains("username");
    }

    // ================= READ APIs =================

    @Cacheable(value = "userByKeycloakId", key = "#keycloakId")
    public UserResponseDto getUserByKeycloakId(String keycloakId) {
        User user = userRepository.findByKeycloakUserIdAndIsActive(keycloakId, IsActive.ACTIVE)
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

    @Cacheable(value = "userById", key = "#userId", unless = "#result == null")
    public UserResponseDto getUserById(Long userId) {

        log.info("DB HIT → fetching userId={}", userId);

        User user = userRepository
                .findByUserIdAndIsActive(userId, IsActive.ACTIVE)
                .orElseThrow(() -> new UserNotFound("User not found"));

        return userMapper.toDto(user);
    }

    // ================= STATUS =================
    @Transactional
    public void deactivateUser(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFound("User not found"));

        if (user.getIsActive() == IsActive.INACTIVE) return;

        user.setIsActive(IsActive.INACTIVE);

        identityProviderService.disableUser(user.getKeycloakUserId());

        cacheEvict(user);
    }

    @Transactional
    public void reactivateUser(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFound("User not found"));

        if (user.getIsActive() == IsActive.ACTIVE) return;

        user.setIsActive(IsActive.ACTIVE);

        identityProviderService.enableUser(user.getKeycloakUserId());

        cacheEvict(user);
    }

    private void cacheEvict(User user) {
        cacheManager.getCache("userById").evict(user.getUserId());
        cacheManager.getCache("userByUsername").evict(user.getUserName());
    }
}