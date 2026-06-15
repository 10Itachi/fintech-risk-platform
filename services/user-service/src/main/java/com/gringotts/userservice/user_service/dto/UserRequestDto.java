package com.gringotts.userservice.user_service.dto;

import com.gringotts.userservice.user_service.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class UserRequestDto {

    @NotBlank(message = "Username is mandatory")
    private String userName;

    @NotBlank(message = "Password is required")
    private String password;

    @NotBlank(message = "Email Required")
    @Email
    private String email;

    @NotBlank(message = "Phone Number Required")
    @Pattern(regexp = "^[0-9]{10}$")
    private String phoneNumber;

    @NotNull(message = "Assign role")
    private Role role;
}
