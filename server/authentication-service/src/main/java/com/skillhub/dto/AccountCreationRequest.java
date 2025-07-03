package com.skillhub.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class AccountCreationRequest {
    private String email;
    private String password;
    private String lastname;
    private String firstname;
    private String phone;

}
