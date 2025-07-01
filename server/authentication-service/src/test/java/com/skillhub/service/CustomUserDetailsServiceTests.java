package com.skillhub.service;

import com.skillhub.repository.UserInfoRepository;
import com.skillhub.entity.Role;
import com.skillhub.entity.UserInfo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTests {

    @Mock
    private UserInfoRepository userInfoRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    @Test
    @DisplayName("Return UserDetails when user exists")
    void loadUserByUsername_whenUserExists_shouldReturnUserDetails() {
        String email = "test@skillhub.com";
        UserInfo mockUser = UserInfo.builder()
                .userId(UUID.randomUUID())
                .email(email)
                .password("hashed_password")
                .role(Role.EMPLOYEE)
                .build();

        when(userInfoRepository.findByEmail(email)).thenReturn(Optional.of(mockUser));

        UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);

        assertNotNull(userDetails);
        assertEquals(email, userDetails.getUsername());
        assertEquals("hashed_password", userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_EMPLOYEE")));
    }

    @Test
    @DisplayName("UsernameNotFoundException if user does not exist")
    void loadUserByUsername_whenUserDoesNotExist_shouldThrowException() {
        String email = "nonexistent@skillhub.com";

        when(userInfoRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> {
            customUserDetailsService.loadUserByUsername(email);
        });
    }
}