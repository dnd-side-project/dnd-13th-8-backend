package com.example.demo.global.auth.refresh.service;

import com.example.demo.global.auth.refresh.dto.ValidationResult;
import com.example.demo.global.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RefreshValidatorImpl implements RefreshValidator {

    private final JwtProvider jwt;

    @Override
    public ValidationResult validate(String presentedRefreshJwt) {
        try {
            var jws = jwt.validateRefresh(presentedRefreshJwt);
            String subject = jws.getPayload().getSubject();
            String jti     = jws.getPayload().getId();
            if (subject == null) return ValidationResult.fail();
            return ValidationResult.ok(subject, jti);
        } catch (Exception e) {
            return ValidationResult.fail();
        }
    }
}
