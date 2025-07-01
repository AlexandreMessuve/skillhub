package com.skillhub.util;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTests {

    private JwtUtil jwtUtil;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();

        String testSecret = "c2tpbGxodWItc2VjcmV0LWtleS1mb3Itand0LWF1dGhlbnRpY2F0aW9uLXNlcnZpY2UtbXVzdC1iZS1sb25nLWVub3VnaA==";
        long testExpiration = 3600000;

        ReflectionTestUtils.setField(jwtUtil, "secretKeyString", testSecret);
        ReflectionTestUtils.setField(jwtUtil, "jwtExpiration", testExpiration);
        jwtUtil.init();
        userDetails = new User("testuser@skillhub.com", "password", new ArrayList<>());
    }

    @Test
    @DisplayName("Generate a JWT token for a valid user")
    void testGenerateToken_shouldReturnValidToken() {
        String token = jwtUtil.generateToken(userDetails);

        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    @DisplayName("Extract username from a valid JWT token")
    void testExtractUsername_shouldReturnCorrectUsername() {
        String token = jwtUtil.generateToken(userDetails);
        String extractedUsername = jwtUtil.extractUsername(token);

        assertEquals("testuser@skillhub.com", extractedUsername);
    }

    @Test
    @DisplayName("Valid a JWT token with correct user details")
    void testValidateToken_withValidToken_shouldReturnTrue() {
        String token = jwtUtil.generateToken(userDetails);

        assertTrue(jwtUtil.validateToken(token, userDetails));
    }

    @Test
    @DisplayName("Token invald with wrong user details")
    void testValidateToken_withWrongUsername_shouldReturnFalse() {
        String token = jwtUtil.generateToken(userDetails);
        UserDetails wrongUserDetails = new User("wronguser@skillhub.com", "password", new ArrayList<>());

        assertFalse(jwtUtil.validateToken(token, wrongUserDetails));
    }

    @Test
    @DisplayName("Token invalid with expired time")
    void testValidateToken_withExpiredToken_shouldReturnFalse() throws InterruptedException {
        ReflectionTestUtils.setField(jwtUtil, "jwtExpiration", 1L);
        jwtUtil.init();

        String token = jwtUtil.generateToken(userDetails);

        TimeUnit.MILLISECONDS.sleep(5);
        assertFalse(jwtUtil.validateToken(token, userDetails));
    }

    @Test
    @DisplayName("Extract username from an expired JWT token should throw ExpiredJwtException")
    void testExtractUsername_withExpiredToken_shouldThrowException() throws InterruptedException {
        ReflectionTestUtils.setField(jwtUtil, "jwtExpiration", 1L);

        String token = jwtUtil.generateToken(userDetails);

        TimeUnit.MILLISECONDS.sleep(5);

        assertThrows(ExpiredJwtException.class, () -> {
            jwtUtil.extractUsername(token);
        });
    }

    @Test
    @DisplayName("Extract expiration date from a valid JWT token")
    void testExtractExpiration_shouldReturnFutureDate() {
        String token = jwtUtil.generateToken(userDetails);
        Date expirationDate = jwtUtil.extractExpiration(token);

        assertTrue(expirationDate.after(new Date()));
    }

    @Test
    @DisplayName("Return false when validating token with invalid signature")
    void testValidateToken_withInvalidSignature_shouldReturnFalse() {
        String invalidToken = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0dXNlciIsImV4cCI6MTY3MjUyOTAwMCwiaWF0IjoxNjcyNTI1NDAwfQ.invalidsignature";

        assertFalse(jwtUtil.validateToken(invalidToken, userDetails));

    }
}