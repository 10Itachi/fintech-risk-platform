package com.gringotts.userservice.user_service.controller;

import com.gringotts.userservice.user_service.dto.UserRequestDto;
import com.gringotts.userservice.user_service.dto.UserResponseDto;
import com.gringotts.userservice.user_service.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    //  PUBLIC → Signup (no token required)
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping()
    public ResponseEntity<UserResponseDto> createUser(
            @Valid @RequestBody UserRequestDto request) {

        UserResponseDto response = userService.createUser(request);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }


    // 🔒 AUTHENTICATED USER → PROFILE
    @GetMapping("/profile")
    public ResponseEntity<UserResponseDto> getProfile(Authentication authentication) {
        String username = authentication.getName();
        return ResponseEntity.ok(
                userService.getUserByUsername(username)
        );
    }

    //  ADMIN → GET ALL USERS
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<UserResponseDto>> getUsers(
            @RequestParam(defaultValue = "0") Long lastId,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(userService.getUsersAfterId(lastId, size));
    }

    // ADMIN OR SELF → GET USER BY ID
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.subject")
    @GetMapping("/{userId}")
    public ResponseEntity<UserResponseDto> getUserById(@PathVariable Long userId) {

        return ResponseEntity.ok(userService.getUserById(userId));
    }

    //  ADMIN → DEACTIVATE USER
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{userId}/deactivate")
    public ResponseEntity<String> deactivateUser(@PathVariable Long userId) {

        userService. deactivateUser(userId);
        return ResponseEntity.ok("User deactivated successfully");
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{userId}/activate")
    public ResponseEntity<Void> activate(@PathVariable Long userId) {
        userService.reactivateUser(userId);
        return ResponseEntity.noContent().build();
    }
}