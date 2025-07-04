package com.skillhub.controller;

import com.skillhub.dto.*;
import com.skillhub.entity.UserInfo;
import com.skillhub.service.CustomUserDetailsService;
import com.skillhub.service.EmailService;
import com.skillhub.service.PreAuthTokenService;
import com.skillhub.service.UserInfoService;
import com.skillhub.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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
    private final PreAuthTokenService preAuthTokenService;

    @Autowired
    public AuthController(AuthenticationManager authenticationManager, JwtUtil jwtUtil, UserInfoService userService,
                          CustomUserDetailsService customUserDetailsService, EmailService emailService,
                          PreAuthTokenService preAuthTokenService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userService = userService;
        this.customUserDetailsService = customUserDetailsService;
        this.emailService = emailService;
        this.preAuthTokenService = preAuthTokenService;
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

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> authenticate(@Validated @RequestBody AuthenticationRequest request) {
        UserInfo user;
        AuthenticationResponse response = new AuthenticationResponse();
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken((request.getEmail()), request.getPassword())
            );
        } catch (BadCredentialsException e) {
            response.setError(e.getMessage() + "Incorrect email or password");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

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

        if(user.is2faEnabled()){
            String preAuthToken = preAuthTokenService.generateToken(user.getEmail());
            response.setMessage("Please enter your 2fa code or use backup code");
            response.setPreAuthToken(preAuthToken);
            response.set2faEnabled(true);
            return ResponseEntity.ok(response);
        }

        return generateJwtResponse(user.getEmail());
    }

    @PostMapping("/login/verify")
    public ResponseEntity<AuthenticationResponse> authWithBackupCode(@Validated @RequestBody TwoFactorRequest twoFactorRequest) {
        AuthenticationResponse response = new AuthenticationResponse();
        if (!preAuthTokenService.isValidToken(twoFactorRequest.getEmail(), twoFactorRequest.getPreAuthToken())){
            response.setError("Invalid pre-auth token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        if (userService.verifySecondFactor(twoFactorRequest.getEmail(), twoFactorRequest.getCode())) {
            return generateJwtResponse(twoFactorRequest.getEmail());
        }
        response.setError("Invalid code");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<Map<String, String>> resendVerificationEmail(@RequestParam String email) {
        if (email.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Email must not be empty"));
        }
        try {
            UserInfo user = userService.getUserByEmail(email);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "User not found"));
            }
            if (user.isVerified()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "User is already verified"));
            }
            emailService.sendVerifyEmailHtml(user);
            return ResponseEntity.ok(Map.of("message", "Verification email sent successfully"));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "An error occurred while resending the verification email."));
        }
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

    public ResponseEntity<AuthenticationResponse> generateJwtResponse(String email) {
        AuthenticationResponse response = new AuthenticationResponse();
        final String jwt = jwtUtil.generateToken(customUserDetailsService.loadUserByUsername(email));
        if (jwt == null) {
            response.setError("Failed to generate JWT token");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
        response.setJwt(jwt);
        return ResponseEntity.ok(response);
    }
}