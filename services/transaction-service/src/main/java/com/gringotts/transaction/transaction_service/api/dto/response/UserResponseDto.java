package com.gringotts.transaction.transaction_service.api.dto.response;

import com.gringotts.transaction.transaction_service.security.iam.IsActive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@AllArgsConstructor
@Getter
public class UserResponseDto {

    private Long userId;
    private String userName;
    private IsActive isActive;

}
