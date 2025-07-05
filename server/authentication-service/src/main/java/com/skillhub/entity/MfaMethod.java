package com.skillhub.entity;

public enum MfaMethod {
    NONE,
    SMS,
    EMAIL,
    TOTP;

    @Override
    public String toString() {
        return switch (this) {
            case SMS -> "SMS";
            case EMAIL -> "Email";
            case TOTP -> "TOTP";
            default -> "None";
        };
    }

}
