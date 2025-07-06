package com.skillhub.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
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

    @NotBlank(message = "MFA code cannot be blank")
    @Pattern(regexp = "^[0-9]{6}$", message = "MFA code must be a 6-digit number")
    private String mfaCode;
}
