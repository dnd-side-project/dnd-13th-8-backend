package com.example.common.error.code;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;

public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String code,
        String message,
        String path,
        String method,
        Map<String, Object> details
) {
    public static ErrorResponse of(
            ErrorCode ec,
            String message,
            String path,
            String method,
            Map<String, Object> details
    ) {
        if (ec == null) {
            throw new IllegalArgumentException("에러 코드(ErrorCode)는 null일 수 없습니다.");
        }

        String resolvedMessage = message;
        if (resolvedMessage == null || resolvedMessage.isBlank()) {
            resolvedMessage = ec.message();
            if (resolvedMessage == null || resolvedMessage.isBlank()) {
                resolvedMessage = "오류가 발생했습니다.";
            }
        }

        Map<String, Object> safeDetails = details;
        if (safeDetails == null) {
            safeDetails = Collections.emptyMap();
        } else {
            safeDetails = Collections.unmodifiableMap(safeDetails);
        }

        return new ErrorResponse(
                LocalDateTime.now(),
                ec.status().value(),
                ec.code(),
                resolvedMessage,
                path,
                method,
                safeDetails
        );
    }
}
