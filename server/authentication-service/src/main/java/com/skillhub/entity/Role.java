package com.skillhub.entity;

public enum Role{
    ADMIN,PDG,RH,EMPLOYEE,USER;

    @Override
    public String toString() {
        return switch (this) {
            case ADMIN -> "ADMIN";
            case RH -> "RH";
            case PDG -> "PDG";
            case EMPLOYEE -> "EMPLOYEE";
            case USER -> "USER";
        };
    }
}
