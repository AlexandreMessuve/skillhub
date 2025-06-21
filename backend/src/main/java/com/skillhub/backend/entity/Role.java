package com.skillhub.backend.entity;

import org.springframework.security.core.GrantedAuthority;

public enum Role implements GrantedAuthority {
    ADMIN,RH,COLLABORATOR;

    @Override
    public String toString() {
        return switch (this) {
            case ADMIN -> "ADMIN";
            case RH -> "RH";
            case COLLABORATOR -> "COLLABORATOR";
        };
    }

    @Override
    public String getAuthority() {
        return toString();
    }
}
