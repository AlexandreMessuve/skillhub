package com.skillhub.backend.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;



@Data
@AllArgsConstructor
public class LoginUserPost {
    private String email;
    private String password;
}
