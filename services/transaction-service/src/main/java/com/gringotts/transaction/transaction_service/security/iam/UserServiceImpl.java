package com.gringotts.transaction.transaction_service.security.iam;

import com.gringotts.transaction.transaction_service.api.dto.request.UserRequestDto;
import com.gringotts.transaction.transaction_service.api.dto.response.UserResponseDto;
import com.gringotts.transaction.transaction_service.api.exception.UserAlreadyExistWithPhoneNumber;
import com.gringotts.transaction.transaction_service.api.exception.UserListEmpty;
import com.gringotts.transaction.transaction_service.api.exception.UserNotFound;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


/* implement all the cases 0of account isActive as of now only two are implemented closed and active*/

@Service
public class UserServiceImpl implements UserService {
   private final UserRepository userRepository;
   private final UserMapper userMapper;

   private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserMapper userMapper, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.userRepository = userRepository;

        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public UserResponseDto createUser(UserRequestDto userRequestDto) {
        Optional<User> user = userRepository.findByPhoneNumber(userRequestDto.getPhoneNumber());
        if (user.isPresent()) {
            User exisitingUser = user.get();
            if(IsActive.ACTIVE.equals(exisitingUser.getIsActive())){
                throw new UserAlreadyExistWithPhoneNumber("User already exist with phone number " + userRequestDto.getPhoneNumber());
            }

            exisitingUser.setPasswordHash(passwordEncoder.encode(userRequestDto.getPasswordHash()));
            exisitingUser.setIsActive(IsActive.ACTIVE);
            exisitingUser.setCreatedAt(LocalDateTime.now());
            return userMapper.toDto(userRepository.save(exisitingUser));
        }
        User newUser = userMapper.toEntity(userRequestDto);
        newUser.setPasswordHash(passwordEncoder.encode(userRequestDto.getPasswordHash()));
        newUser.setIsActive(IsActive.ACTIVE);
        newUser.setCreatedAt(LocalDateTime.now());
        return userMapper.toDto(userRepository.save(newUser));
    }



    @Override
    @Transactional
    public UserResponseDto findByUserId(Long userId ) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFound("User not found with id " + userId));
        if(!IsActive.ACTIVE.equals(user.getIsActive())){
            throw new UserNotFound("User not found with id " + userId);
        }
        return userMapper.toDto(user);

    }

    @Override
    @Transactional
    public UserResponseDto findByUserName(String userName ) {
        User user = userRepository.findByUserName(userName).orElseThrow(() -> new UserNotFound("User not found with name " + userName));
        return userMapper.toDto(user);
    }

    @Override
    @Transactional
    public List<UserResponseDto> findAllUsers() {
        List<User> userList= userRepository.findAll();
        if(userList.isEmpty()){
            throw new UserListEmpty("UserList is empty");
        }
        List<UserResponseDto> userResponse = userList.stream()
                .map(user -> userMapper.toDto(user))
                .collect(Collectors.toList());
        return userResponse;
    }

    @Override
    @Transactional
    public void deleteUserById(Long userId) {
        Optional<User> existingUser = userRepository.findById(userId);
        if(existingUser.isPresent()){
            User exxstingUser = existingUser.get();
            if(IsActive.CLOSED.equals(exxstingUser.getIsActive())){
                throw new UserNotFound("user with id " + userId + " already closed");
            }
        }
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("user not found"));
        user.setIsActive(IsActive.CLOSED);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public UserResponseDto updateActiveStatus(Long userId, IsActive active) {
        User user = userRepository.findByUserId(userId).orElseThrow(() -> new UserNotFound("User not found with id " + userId));
        user.setIsActive(IsActive.ACTIVE);
        return userMapper.toDto(userRepository.save(user));
    }
}
