package com.skillhub.backend.dto.user;

import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;



@Data
@AllArgsConstructor
public class RegisterUserPost {
    @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$", message = "Email must be a valid email address.")
    private String email;

    @Pattern(regexp = "^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[0-9])(?=.*?[#?!@$%^&*-]).{6,30}$", message = "Password must be between 6 and 30 characters long, include at least one uppercase letter, one lowercase letter, one digit, and one special character.")
    private String password;
    private String confirmPassword;
}
