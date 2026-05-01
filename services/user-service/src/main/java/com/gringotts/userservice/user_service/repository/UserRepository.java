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

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUserIdAndIsActive(Long userId, IsActive isActive);

    Optional<User> findByUserNameAndIsActive(String userName, IsActive isActive);

    Optional<User> findByUserName(String userName);

    Optional<User> findByEmail(String email);

    Optional<User> findByPhoneNumber(String phoneNumber);

    List<User> findByUserIdGreaterThanOrderByUserIdAsc(Long lastId, Pageable pageable);
}
