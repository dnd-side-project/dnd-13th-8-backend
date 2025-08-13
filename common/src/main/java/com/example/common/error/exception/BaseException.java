package com.example.common.error.exception;

import com.example.common.error.code.ErrorCode;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 모든 도메인/서비스 예외의 루트.
 * - 상태/코드/기본메시지는 ErrorCode가 보유
 * - details에는 추가 컨텍스트(식별자, 필드명 등)를 담아 전달
 */
public class BaseException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    private final ErrorCode errorCode;
    private final Map<String, Object> details;

    /** ErrorCode의 기본 메시지로 예외 생성 */
    public BaseException(ErrorCode errorCode) {
        super(requireAndGetDefaultMessage(errorCode));
        this.errorCode = errorCode;
        this.details = Collections.emptyMap();
    }

    /** 커스텀 메시지로 예외 생성 */
    public BaseException(String message, ErrorCode errorCode) {
        super(resolveMessage(message, errorCode));
        this.errorCode = requireNonNull(errorCode);
        this.details = Collections.emptyMap();
    }

    /** ErrorCode 기본 메시지 + 세부정보(details) 포함 */
    public BaseException(ErrorCode errorCode, Map<String, Object> details) {
        super(requireAndGetDefaultMessage(errorCode));
        this.errorCode = errorCode;
        if (details == null) {
            this.details = Collections.emptyMap();
        } else {
            this.details = Collections.unmodifiableMap(new HashMap<>(details));
        }
    }

    /** 커스텀 메시지 + 세부정보(details) 포함 */
    public BaseException(String message, ErrorCode errorCode, Map<String, Object> details) {
        super(resolveMessage(message, errorCode));
        this.errorCode = requireNonNull(errorCode);
        if (details == null) {
            this.details = Collections.emptyMap();
        } else {
            this.details = Collections.unmodifiableMap(new HashMap<>(details));
        }
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public Map<String, Object> getDetails() {
        return details;
    }

    /* ===== 내부 헬퍼 ===== */

    private static ErrorCode requireNonNull(ErrorCode errorCode) {
        if (errorCode == null) {
            throw new IllegalArgumentException("에러 코드(ErrorCode)는 null일 수 없습니다.");
        }
        return errorCode;
    }

    private static String requireAndGetDefaultMessage(ErrorCode errorCode) {
        if (errorCode == null) {
            throw new IllegalArgumentException("에러 코드(ErrorCode)는 null일 수 없습니다.");
        }
        String message = errorCode.message();
        if (message == null || message.isBlank()) {
            return "오류가 발생했습니다.";
        }
        return message;
    }

    private static String resolveMessage(String message, ErrorCode errorCode) {
        if (message != null && !message.isBlank()) {
            return message;
        }
        return requireAndGetDefaultMessage(errorCode);
    }
}