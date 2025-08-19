package com.example.demo.global.auth.refresh.service;

import com.example.demo.global.auth.refresh.dto.IssuedTokens;

public interface TokenIssuer {
    IssuedTokens preIssue(String userId);
}
