package com.example.demo.global.auth.refresh.dto;

public record ValidationResult(
        boolean valid,
        String userId,
        String presentedJti
) {
    public static ValidationResult ok(String userId, String jti) {
        return new ValidationResult(true, userId, jti);
    }

    public static ValidationResult fail() {
        return new ValidationResult(false, null, null);
    }
}
