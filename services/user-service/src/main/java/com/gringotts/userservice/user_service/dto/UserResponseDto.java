package com.gringotts.userservice.user_service.dto;

import com.gringotts.userservice.user_service.enums.IsActive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class UserResponseDto {

    private UUID userId;
    private String userName;
    private IsActive isActive;

}
