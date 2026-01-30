package com.gringotts.transaction.transaction_service.api.dto.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Getter
@Builder
public class JwtAuthResponseDto {
    private String accessToken;
    private String tokenType;
    private long expiresIn;
    private long issuedAt;

    public JwtAuthResponseDto() {

    }
}
