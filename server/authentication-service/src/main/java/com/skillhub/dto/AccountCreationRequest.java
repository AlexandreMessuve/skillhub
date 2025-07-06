package com.skillhub.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Validated
public class AccountCreationRequest {
    @NotBlank(message = "Email cannot be null")
    @Pattern(regexp = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$", message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password cannot be null")
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
            message = "Password must be at least 8 characters long, contain at least one uppercase letter," +
                    "one lowercase letter, one digit, and one special character.")
    private String password;

    @NotBlank(message = "Last name cannot be null")
    private String lastname;

    @NotBlank(message = "First name cannot be null")
    private String firstname;

    @NotBlank(message = "Phone number cannot be null")
    @Pattern(regexp = "^\\+[1-9]\\d{1,14}$", message = "Invalid phone number format, must be in E.164 format (e.g., +1234567890)")
    private String phone;

}
