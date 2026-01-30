package com.gringotts.transaction.transaction_service.api.dto.request;


import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Setter
@Getter
public class UserLoginRequest {
    //UserLoginRequest
    @NotNull(message = "Phone Number Required")
    @Pattern(regexp = "^[0-9]{10}$")
    private String phoneNumber;
    @NotNull(message = "Password is required")
    private String password;
}
