package com.skillhub.security;

import com.skillhub.entity.Role;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

@Getter
public class CustomUserDetails implements UserDetails {
    private final UUID userId;
    private final String email;
    private final String password;
    private final Role role;
    private final String firstName;
    private final String lastName;
    private final Collection<? extends GrantedAuthority> authorities;

    public CustomUserDetails(UUID userId, String email, String password, Role role, String firstName, String lastName) {
        this.userId = userId;
        this.email = email;
        this.password = password;
        this.role = role;
        this.authorities = Collections.<GrantedAuthority>singletonList(new SimpleGrantedAuthority("ROLE_" + role.name()));
        this.firstName = firstName;
        this.lastName = lastName;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
