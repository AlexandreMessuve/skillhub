package com.skillhub.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Validated
public class TwoFactorRequest {
    @NotBlank(message = "Email cannot be blank")
    @Email
    private String email;

    @NotBlank(message = "Code cannot be blank")
    @Pattern(regexp = "\\d{6}", message = "Code must be in the format 'XXXXXX' where X is a digit")
    @Size(min = 6, max = 6, message = "Code must be exactly 6 digits long")
    private String code;

    @NotBlank(message = "Token cannot be blank")
    private String preAuthToken;
}
