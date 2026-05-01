package com.gringotts.userservice.user_service.dto;

import com.gringotts.userservice.user_service.enums.IsActive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class UserResponseDto {

    private Long userId;
    private String userName;
    private IsActive isActive;

}
