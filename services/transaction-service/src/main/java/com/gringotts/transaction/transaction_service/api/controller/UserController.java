package com.gringotts.transaction.transaction_service.api.controller;

import com.gringotts.transaction.transaction_service.api.dto.request.UserRequestDto;
import com.gringotts.transaction.transaction_service.api.dto.response.UserResponseDto;
import com.gringotts.transaction.transaction_service.security.iam.IsActive;
import com.gringotts.transaction.transaction_service.security.iam.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/addUser")
    public ResponseEntity<UserResponseDto> createUser(@RequestBody @Valid UserRequestDto userRequestDto){
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(userRequestDto));
    }
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.userId")
    @GetMapping("/getUserById/{userId}")
    public ResponseEntity<UserResponseDto> getUserById(@PathVariable Long userId){
        UserResponseDto userResponseDto =userService.findByUserId(userId);
        return ResponseEntity.status(HttpStatus.OK).body(userResponseDto);
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/getUserByName/{userName}")
    public ResponseEntity<UserResponseDto> getUserByName(@PathVariable String userName){
        return ResponseEntity.status(HttpStatus.OK).body(userService.findByUserName(userName));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/getAllUsers")
    public ResponseEntity<List<UserResponseDto>> getAllUsers(){
        return ResponseEntity.status(HttpStatus.OK).body(userService.findAllUsers());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/deleteUserById/{userId}")
    public ResponseEntity<Void> deleteUserById(@PathVariable Long userId){
        userService.deleteUserById(userId);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/updateActiveStatus/{userId}/status")
    public ResponseEntity<UserResponseDto> updateActiveStatus(@PathVariable Long userId, @RequestParam IsActive active){
        UserResponseDto userResponseDto = userService.updateActiveStatus(userId,active);
        return ResponseEntity.ok(userResponseDto);
    }

}
