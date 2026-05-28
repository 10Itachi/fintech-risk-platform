package com.gringotts.userservice.user_service.controller;

import com.gringotts.userservice.user_service.dto.UserRequestDto;
import com.gringotts.userservice.user_service.dto.UserResponseDto;
import com.gringotts.userservice.user_service.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    //  PUBLIC → Signup (no token required)
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/createUser")
    public ResponseEntity<UserResponseDto> createUser(
            @Valid @RequestBody UserRequestDto request) {

        UserResponseDto response = userService.createUser(request);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }


    // 🔒 AUTHENTICATED USER → PROFILE
    @GetMapping("/user/profile")
    public ResponseEntity<UserResponseDto> getProfile(JwtAuthenticationToken authentication) {
        String keycloakId = authentication.getToken().getSubject();
        return ResponseEntity.ok(
                userService.getUserByKeycloakId(keycloakId)
        );
    }

    //  ADMIN → GET ALL USERS
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/getUsers")
    public ResponseEntity<List<UserResponseDto>> getUsers(
            @RequestParam(defaultValue = "0") UUID lastId,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(userService.getUsersAfterId(lastId, size));
    }

    // ADMIN OR SELF → GET USER BY ID
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @GetMapping("/shared/getUserById/{userId}")
    public ResponseEntity<UserResponseDto> getUserById(@PathVariable UUID userId) {

        return ResponseEntity.ok(userService.getUserById(userId));
    }

    //  ADMIN → DEACTIVATE USER
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/admin/{userId}/deactivate")
    public ResponseEntity<String> deactivateUser(@PathVariable UUID userId) {

        userService. deactivateUser(userId);
        return ResponseEntity.ok("User deactivated successfully");
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/admin/{userId}/activate")
    public ResponseEntity<Void> activate(@PathVariable UUID userId) {
        userService.reactivateUser(userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/shared/ping")
    public String ping() {
        return "PING OK";
    }
}