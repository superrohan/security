package com.bank.capp.models;

import com.bank.capp.constants.ApiConstants;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {

    @NotBlank(message = ApiConstants.USERNAME_REQUIRED)
    @Size(min = 3, max = 50, message = ApiConstants.USERNAME_SIZE)
    private String username;

    @NotBlank(message = ApiConstants.EMAIL_REQUIRED)
    @Email(message = ApiConstants.EMAIL_INVALID)
    private String email;

    @NotBlank(message = ApiConstants.PASSWORD_REQUIRED)
    @Size(min = 8, message = ApiConstants.PASSWORD_MIN_LENGTH)
    private String password;

    private String firstName;
    
    private String lastName;
}
