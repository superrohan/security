package com.bank.capp.models;

import com.bank.capp.constants.ApiConstants;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthenticationRequest {

    @NotBlank(message = ApiConstants.USERNAME_REQUIRED)
    private String username;

    @NotBlank(message = ApiConstants.PASSWORD_REQUIRED)
    private String password;
}
