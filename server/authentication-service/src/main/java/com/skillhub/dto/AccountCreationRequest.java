package com.skillhub.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class AccountCreationRequest {
    private String password;
    private String lastname;
    private String firstname;
    private String email;
}
