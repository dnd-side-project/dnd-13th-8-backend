package com.example.demo.global.auth.refresh.service;

import com.example.demo.global.auth.refresh.dto.ValidationResult;

public interface RefreshValidator {

    ValidationResult validate(String presentedRefreshJwt);
}
