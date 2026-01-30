package com.gringotts.transaction.transaction_service.api.dto.request;

import com.gringotts.transaction.transaction_service.security.iam.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class UserRequestDto {

    @NotNull(message = "Username is mandatory")
    private String userName;

    @NotNull(message = "Set Password")
    private String passwordHash;

    @NotNull(message = "Email Required")
    @Email
    private String email;

    @NotNull(message = "Phone Number Required")
    @Pattern(regexp = "^[0-9]{10}$")
    private String phoneNumber;

    @NotNull(message = "Assign role")
    private Role role;

}
