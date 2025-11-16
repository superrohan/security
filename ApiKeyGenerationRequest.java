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
public class ApiKeyGenerationRequest {

    @NotBlank(message = ApiConstants.SERVICE_NAME_REQUIRED)
    private String serviceName;

    @NotBlank(message = ApiConstants.DESCRIPTION_REQUIRED)
    private String description;
}
