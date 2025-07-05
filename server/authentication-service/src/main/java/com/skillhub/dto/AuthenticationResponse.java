package com.skillhub.dto;

import com.skillhub.entity.MfaMethod;
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
    private MfaMethod mfaMethod;
}