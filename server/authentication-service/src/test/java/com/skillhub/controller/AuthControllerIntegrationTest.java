package com.skillhub.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skillhub.dto.AccountCreationRequest;
import com.skillhub.dto.AuthenticationRequest;
import com.skillhub.service.CustomUserDetailsService;
import com.skillhub.service.UserInfoService;
import com.skillhub.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserInfoService userService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @BeforeEach
    void setUp() {

    }

    @Test
    void signup_whenValidRequest_shouldReturnCreated() throws Exception {
        AccountCreationRequest request = new AccountCreationRequest("John", "Doe", "john.doe@email.com", "password123","00-00-0000");

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void signup_whenEmailAlreadyExists_shouldReturnConflict() throws Exception {
        AccountCreationRequest request = new AccountCreationRequest("Jane", "Doe", "jane.doe@email.com", "password123","00-00-0000");

        doThrow(new IllegalStateException("Email already in use"))
                .when(userService).registerNewUser(any(), any(), eq("jane.doe@email.com"), any(), any());

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void authenticate_whenCredentialsAreValid_shouldReturnOkAndToken() throws Exception {
        AuthenticationRequest request = new AuthenticationRequest("user@email.com", "password");
        UserDetails userDetails = new User("user@email.com", "encoded-password", new ArrayList<>());
        String fakeJwt = "fake-jwt-token";

        when(customUserDetailsService.loadUserByUsername(request.getEmail())).thenReturn(userDetails);
        when(jwtUtil.generateToken(userDetails)).thenReturn(fakeJwt);

        mockMvc.perform(post("/api/auth/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jwt").value(fakeJwt));
    }

    @Test
    void authenticate_whenCredentialsAreInvalid_shouldReturnUnauthorized() throws Exception {
        AuthenticationRequest request = new AuthenticationRequest("user@email.com", "wrong-password");

        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Incorrect email or password"));

        mockMvc.perform(post("/api/auth/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
}