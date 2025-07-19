package com.skillhub.dto;

import jakarta.validation.constraints.NotBlank;
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
public class AccountVerifyPhoneRequest {
    @NotBlank(message = "Code cannot be blank")
    @Pattern(regexp = "^[0-9]{6}$", message = "Code must be a 6-digit number")
    private String code;
}
