package com.skillhub.controller;

import com.skillhub.entity.UserInfo;
import com.skillhub.dto.AccountCreationRequest;
import com.skillhub.dto.AuthenticationRequest;
import com.skillhub.dto.AuthenticationResponse;
import com.skillhub.service.CustomUserDetailsService;
import com.skillhub.service.UserInfoService;
import com.skillhub.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserInfoService userService;
    private final CustomUserDetailsService customUserDetailsService;

    @Autowired
    public AuthController(AuthenticationManager authenticationManager, JwtUtil jwtUtil, UserInfoService userService,
                          CustomUserDetailsService customUserDetailsService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userService = userService;
        this.customUserDetailsService = customUserDetailsService;
    }

    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("success");
    }

    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody AccountCreationRequest request) {
        try {
            userService.registerNewUser(
                    request.getFirstname(),
                    request.getLastname(),
                    request.getEmail(),
                    request.getPassword(),
                    request.getEmail()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body("User registered successfully");
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("An error occurred during registration.");
        }
    }

    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest request) {
        if (request.getEmail().isEmpty() || request.getPassword().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email and password must not be empty");
        }
        UserInfo user = userService.getUserByEmail(request.getEmail());

        if (!user.isVerified()){
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(AuthenticationResponse.builder().error("Account not verified").build());
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken((request.getEmail()), request.getPassword())
            );
        } catch (BadCredentialsException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Incorrect email or password", e);
        }

        final UserDetails userDetails = customUserDetailsService.loadUserByUsername(request.getEmail());
        final String jwt = jwtUtil.generateToken(userDetails);
        if (jwt == null) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to generate JWT token");
        }
        AuthenticationResponse authenticationResponse = AuthenticationResponse.builder()
                .jwt("Bearer " + jwt)
                .build();
        return ResponseEntity.ok(authenticationResponse);
    }


    @GetMapping("/verify")
    public ResponseEntity<String> verifyAccount(@RequestParam String token) {
        boolean isVerified = userService.verifyUser(token);
        if (isVerified) {
            return ResponseEntity.ok("Compte vérifié avec succès !");
        } else {
            return ResponseEntity.badRequest().body("Le jeton de vérification est invalide ou a expiré.");
        }
    }
}