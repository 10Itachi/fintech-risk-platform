package com.gringotts.transaction.transaction_service.security.iam;

import com.gringotts.transaction.transaction_service.api.dto.request.UserRequestDto;
import com.gringotts.transaction.transaction_service.api.dto.response.UserResponseDto;

import java.util.List;

public interface UserService {
    UserResponseDto createUser(UserRequestDto userRequestDto);
    UserResponseDto findByUserId(Long userId);
    UserResponseDto findByUserName(String userName);
    List<UserResponseDto> findAllUsers();
     void deleteUserById(Long userId);

    UserResponseDto updateActiveStatus(Long userId, IsActive active);
}
