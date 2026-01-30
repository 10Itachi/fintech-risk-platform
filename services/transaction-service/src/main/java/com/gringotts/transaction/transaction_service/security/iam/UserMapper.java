package com.gringotts.transaction.transaction_service.security.iam;

import com.gringotts.transaction.transaction_service.api.dto.request.UserRequestDto;
import com.gringotts.transaction.transaction_service.api.dto.response.UserResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "createdAt" , ignore = true)
    @Mapping(target = "isActive", ignore = true)
    User toEntity(UserRequestDto dto);
    UserResponseDto toDto(User entity);
}
