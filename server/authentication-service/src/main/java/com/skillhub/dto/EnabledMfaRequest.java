package com.skillhub.dto;

import com.skillhub.entity.MfaMethod;
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
public class EnabledMfaRequest {
    @NotNull(message = "mfaMethod cannot be null, for exemple: SMS, EMAIL, TOTP")
    private MfaMethod mfaMethod;

    @NotBlank(message = "MFA code cannot be blank")
    @Pattern(regexp = "^[0-9]{6}$", message = "MFA code must be a 6-digit number")
    private String mfaCode;
}
