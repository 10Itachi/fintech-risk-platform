package com.gringotts.transaction.transaction_service.security.iam;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
Optional<User> findByUserIdAndIsActive(Long userId, IsActive isActive);
Optional<User> findByUserNameAndIsActive(String userName, IsActive isActive);
Optional<User> findByPhoneNumber(@NotNull(message = "Phone Number Required") @Pattern(regexp = "^[0-9]{10}$") String phoneNumber);
Optional<User> findByUserId(Long userId);
Optional<User> findByUserName(String userName);
boolean existsByRole(Role role);
}
