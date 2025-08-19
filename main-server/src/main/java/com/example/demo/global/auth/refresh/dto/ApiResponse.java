package com.example.demo.global.auth.refresh.dto;


public record ApiResponse<T>(
        String status,
        T data
) {
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>("ok", data);
    }
    public static <T> ApiResponse<T> error(String message) {
        return (ApiResponse<T>) new ApiResponse<>("error", message);
    }
}

