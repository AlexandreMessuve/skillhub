package com.skillhub.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Validated
public class MfaAuthRequest {
    @Email
    @NotBlank(message = "Email cannot be blank")
    private String email;

    @NotBlank(message = "Pre-authentication token cannot be blank")
    private String preAuthToken;

    @NotNull(message = "mfaMethod cannot be null, for exemple: SMS, EMAIL, TOTP")
    private String mfaCode;
}
