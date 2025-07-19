package com.skillhub.controller;

import com.skillhub.dto.*;
import com.skillhub.entity.MfaMethod;
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

/**
 * Controller for handling authentication-related requests.
 * This includes user signup, login, account verification, and 2FA.
 */
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

    /**
     * Constructor for AuthController with dependency injection.
     *
     * @param authenticationManager Manages the authentication process.
     * @param jwtUtil Utility for generating and validating JWTs.
     * @param userService Service for user-related operations.
     * @param customUserDetailsService Service for loading user-specific data.
     * @param emailService Service for sending emails.
     * @param preAuthTokenService Service for managing pre-authentication tokens for 2FA.
     */
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

    /**
     * Handles the user registration process.
     * Creates a new user account and sends a verification email.
     *
     * @param request The request body containing user details (firstname, lastname, email, password, phone).
     * @return A ResponseEntity indicating the result of the registration attempt.
     */
    @PostMapping("/signup")
    public ResponseEntity<AccountCreationResponse> signup(@Validated @RequestBody AccountCreationRequest request) {
        AccountCreationResponse response = new AccountCreationResponse();
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

    /**
     * Authenticates a user based on email and password.
     * If credentials are correct, it checks if the account is verified.
     * If 2FA is enabled, it returns a pre-authentication token.
     * Otherwise, it returns a JWT.
     *
     * @param request The request body containing the user's email and password.
     * @return A ResponseEntity containing a JWT, a 2FA prompt, or an error message.
     */
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

        if(user.getPreferedMfaMethod() != MfaMethod.NONE){
            String preAuthToken = preAuthTokenService.generateToken(user.getEmail());
            response.setMessage("Please enter your 2fa code or use backup code");
            response.setMfaMethod(user.getPreferedMfaMethod());
            response.setPreAuthToken(preAuthToken);
            response.set2faEnabled(true);
            if (user.getPreferedMfaMethod() == MfaMethod.EMAIL || user.getPreferedMfaMethod() == MfaMethod.SMS) {
                userService.reSendMfaCode(user.getEmail());
            }
            return ResponseEntity.ok(response);
        }

        return generateJwtResponse(user.getEmail());
    }

    /**
     * Verifies the two-factor authentication code provided by the user.
     *
     * @param mfaAuthRequest The request body containing the email, pre-auth token, and 2FA code.
     * @return A ResponseEntity containing a JWT upon successful verification, or an error otherwise.
     */
    @PostMapping("/login/verify")
    public ResponseEntity<AuthenticationResponse> auth2faCode(@Validated @RequestBody MfaAuthRequest mfaAuthRequest) {
        AuthenticationResponse response = new AuthenticationResponse();
        if (!preAuthTokenService.isValidToken(mfaAuthRequest.getEmail(), mfaAuthRequest.getPreAuthToken())){
            response.setError("Invalid pre-auth token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        if (userService.verifySecondFactor(mfaAuthRequest.getEmail(), mfaAuthRequest.getMfaCode())) {
            return generateJwtResponse(mfaAuthRequest.getEmail());
        }
        response.setError("Invalid code");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    /**
     * Resends the verification email to a user who has not yet verified their account.
     *
     * @param email The email address of the user.
     * @return A ResponseEntity indicating the outcome of the request.
     */
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

    /**
     * Verifies a user's account using a token sent to their email.
     *
     * @param token The verification token from the email link.
     * @return A ResponseEntity indicating the outcome of the verification.
     */
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

    /**
     * Helper method to generate a ResponseEntity containing a JWT.
     *
     * @param email The user's email for whom the token is generated.
     * @return A ResponseEntity containing the JWT or an error.
     */
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