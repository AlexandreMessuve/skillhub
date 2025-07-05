package com.skillhub.dto;

import com.skillhub.entity.MfaMethod;
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
public class SetupMfaRequest {
    @NotNull(message = "mfaMethod cannot be null, for exemple: SMS, EMAIL, TOTP")
    private MfaMethod mfaMethod;
}
