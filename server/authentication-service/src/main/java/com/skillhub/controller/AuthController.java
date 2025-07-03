package com.skillhub.controller;

import com.skillhub.dto.AccountCreationResponse;
import com.skillhub.entity.UserInfo;
import com.skillhub.dto.AccountCreationRequest;
import com.skillhub.dto.AuthenticationRequest;
import com.skillhub.dto.AuthenticationResponse;
import com.skillhub.service.CustomUserDetailsService;
import com.skillhub.service.EmailService;
import com.skillhub.service.UserInfoService;
import com.skillhub.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserInfoService userService;
    private final CustomUserDetailsService customUserDetailsService;
    private final EmailService emailService;

    @Autowired
    public AuthController(AuthenticationManager authenticationManager, JwtUtil jwtUtil, UserInfoService userService,
                          CustomUserDetailsService customUserDetailsService, EmailService emailService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userService = userService;
        this.customUserDetailsService = customUserDetailsService;
        this.emailService = emailService;
    }

    @GetMapping("/test")
    public ResponseEntity<Map<String, String>> test() {
        Map<String, String> response = Map.of("message", "Hello, this is a test endpoint!");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/signup")
    public ResponseEntity<AccountCreationResponse> signup(@Validated @RequestBody AccountCreationRequest request) {
        AccountCreationResponse response = new AccountCreationResponse();
        // This method handles user registration
        try {
            UserInfo user = userService.registerNewUser(
                    request.getFirstname(),
                    request.getLastname(),
                    request.getEmail(),
                    request.getPassword(),
                    request.getPhone()
            );
           emailService.sendVerifyEmailHtml(user);
            response.setAccount_creation_status("Account created successfully. Please check your email to verify your account.");
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalStateException e) {
            response.setError_msg(e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        } catch (Exception e) {
            response.setError_msg("An error occurred during registration.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> authenticate(@Validated @RequestBody AuthenticationRequest request) {
        UserInfo user;
        AuthenticationResponse response = new AuthenticationResponse();
        try {
            user = userService.getUserByEmail(request.getEmail());
        } catch (IllegalStateException e) {
            response.setError(e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        if (!user.isVerified()){
            response.setError("Account not verified");
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(response);
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken((request.getEmail()), request.getPassword())
            );
        } catch (BadCredentialsException e) {
            response.setError(e.getMessage() + "Incorrect email or password");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        final UserDetails userDetails = customUserDetailsService.loadUserByUsername(request.getEmail());
        final String jwt = jwtUtil.generateToken(userDetails);
        if (jwt == null) {
            response.setError("Failed to generate JWT token");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
        response.setJwt(jwt);
        return ResponseEntity.ok(response);
    }


    @GetMapping("/verify")
    public ResponseEntity<Map<String, String>> verifyAccount(@RequestParam String token) {
        if (token.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Token must not be empty"));
        }
        try {
            userService.verifyUser(token);
            return ResponseEntity.ok(Map.of("message", "Account verified successfully"));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "An error occurred during verification."));
        }
    }
}