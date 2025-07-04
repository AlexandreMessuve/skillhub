package com.skillhub.dto;

import lombok.*;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class AuthenticationResponse {
    private String jwt;
    private String error;
    private String message;
    private boolean is2faEnabled;
    private String preAuthToken;
}