package com.gringotts.userservice.user_service.mapper;


import com.gringotts.userservice.user_service.dto.UserRequestDto;
import com.gringotts.userservice.user_service.dto.UserResponseDto;
import com.gringotts.userservice.user_service.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "keycloakUserId", source = "keycloakUserId")
    User toEntity(UserRequestDto dto, String keycloakUserId);

    UserResponseDto toDto(User entity);
}