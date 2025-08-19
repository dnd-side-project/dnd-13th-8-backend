package com.example.demo.global.auth.refresh.service;


import com.example.demo.global.auth.refresh.dto.RefreshResult;

public interface RefreshService {
    RefreshResult refresh(String presentedRefreshJwt, String userId, String sessionId);
}
