package com.gringotts.transaction.transaction_service.api.controller;

import com.gringotts.transaction.transaction_service.api.dto.request.UserLoginRequest;
import com.gringotts.transaction.transaction_service.api.dto.response.JwtAuthResponseDto;
import com.gringotts.transaction.transaction_service.security.jwt.CustomerUserDetails;
import com.gringotts.transaction.transaction_service.security.jwt.JwtAuthenticationHelper;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {
     private final AuthenticationManager authenticationManager;
     private final JwtAuthenticationHelper helper;

    public AuthController(AuthenticationManager authenticationManager, JwtAuthenticationHelper helper) {
        this.authenticationManager = authenticationManager;
        this.helper = helper;
    }

    @PostMapping("/login")
    public ResponseEntity<JwtAuthResponseDto> login(@Valid @RequestBody UserLoginRequest userLoginRequest) {

       /*verification of the user*/
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        userLoginRequest.getPhoneNumber(),
                        userLoginRequest.getPassword()
                )
        );

        /*getting details of verified user*/
        CustomerUserDetails userDetails = (CustomerUserDetails)authentication.getPrincipal();

        /*get token*/
        String token = helper.generateToken(userDetails);

        return ResponseEntity.ok(JwtAuthResponseDto.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresIn(helper.getExpirationInSecond())
                .build());

    }
}
