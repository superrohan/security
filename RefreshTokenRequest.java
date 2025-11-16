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
public class RefreshTokenRequest {

    @NotBlank(message = ApiConstants.REFRESH_TOKEN_REQUIRED)
    private String refreshToken;
}
