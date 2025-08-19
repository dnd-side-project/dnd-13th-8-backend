package com.example.demo.global.auth.refresh.service;

public interface ReuseHandler {
    void onSuspiciousReuse(String userId);
}