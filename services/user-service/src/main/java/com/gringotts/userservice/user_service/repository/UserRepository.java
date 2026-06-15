package com.gringotts.userservice.user_service.repository;

import com.gringotts.userservice.user_service.entity.User;
import com.gringotts.userservice.user_service.enums.IsActive;
import com.gringotts.userservice.user_service.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUserIdAndIsActive(UUID userId, IsActive isActive);
    Optional<User> findByKeycloakUserIdAndIsActive(String keycloakUserId, IsActive isActive);
    List<User> findByUserIdGreaterThanOrderByUserIdAsc(UUID lastId, Pageable pageable);
    Optional<User> findByUserNameAndIsActive(String username, IsActive isActive);

    Optional<Object> findByUserId(UUID userId);
}
