package com.example.demo.global.security.jwt;

public enum JwtRoleType {
    USER("ROLE_USER"),
    SUPER("ROLE_SUPER"),
    ANONYMOUS("ROLE_ANONYMOUS");

    private final String value;

    JwtRoleType(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
