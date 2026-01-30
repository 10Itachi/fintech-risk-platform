/*
package com.gringotts.transaction.transaction_service.Services;


import com.gringotts.transaction.transaction_service.Api.Dto.Request.UserRequestDto;
import com.gringotts.transaction.transaction_service.Api.Dto.Response.UserResponseDto;
import com.gringotts.transaction.transaction_service.Security.Iam.User;
import com.gringotts.transaction.transaction_service.Security.Iam.IsActive;
import com.gringotts.transaction.transaction_service.Security.Iam.Role;
import com.gringotts.transaction.transaction_service.Mapper.UserMapper;
import com.gringotts.transaction.transaction_service.Security.Iam.UserRepository;
import net.bytebuddy.dynamic.TypeResolutionStrategy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void shouldCreateNewUserWithoutExistingPhoneNumber() {
        UserRequestDto userRequestDto = new UserRequestDto();
        userRequestDto.setUserName("Peter");
        userRequestDto.setPasswordHash("12345");
        userRequestDto.setEmail("peter@gmail.com");
        userRequestDto.setPhoneNumber("123456789");

        User userEntity = new User();
        userEntity.setUserName(userRequestDto.getUserName());
        userEntity.setIsActive(IsActive.ACTIVE);
        userEntity.setRole(Role.ADMIN);

        UserResponseDto userResponseDto = UserResponseDto.builder().userId(10L).userName("Peter")
                .isActive(IsActive.ACTIVE).build();

        when(userRepository.findByPhoneNumber("123456789")).thenReturn(Optional.empty());
        when(userMapper.toEntity(userRequestDto)).thenReturn(userEntity);
        when(userRepository.save(any(User.class))).thenReturn(userEntity);
        when(userMapper.toDto(userEntity)).thenReturn(userResponseDto);

        UserResponseDto result = userService.createUser(userRequestDto);

        assertNotNull(result);
        assertEquals(10L, result.getUserId());
        assertEquals("Peter", result.getUserName());
        assertEquals(IsActive.ACTIVE, result.getIsActive());

        verify(userRepository, times(1))
        .findByPhoneNumber("123456789");
        verify(userRepository, times(1)).save(any(User.class));


    }


}
*/
