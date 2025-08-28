package com.example.common.error.code;

import org.springframework.http.HttpStatus;

public enum CommonErrorCode implements ErrorCode {

    // --- 400 Bad Request ---
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "COMMON-400", "잘못된 요청입니다."),
    VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "COMMON-422", "요청 필드 검증에 실패했습니다."),
    TYPE_MISMATCH(HttpStatus.BAD_REQUEST, "COMMON-400-TYPE", "요청 파라미터 타입이 잘못되었습니다."),
    MISSING_PARAMETER(HttpStatus.BAD_REQUEST, "COMMON-400-MISSING", "필수 요청 파라미터가 누락되었습니다."),
    JSON_PARSE_ERROR(HttpStatus.BAD_REQUEST, "COMMON-400-JSON", "요청 본문을 읽을 수 없습니다."),

    // --- 401 Unauthorized ---
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "COMMON-401", "인증이 필요합니다."),

    // --- 403 Forbidden ---
    FORBIDDEN(HttpStatus.FORBIDDEN, "COMMON-403", "접근 권한이 없습니다."),

    // --- 404 Not Found ---
    NOT_FOUND(HttpStatus.NOT_FOUND, "COMMON-404", "리소스를 찾을 수 없습니다."),

    // --- 405 Method Not Allowed ---
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "COMMON-405", "허용되지 않은 메서드입니다."),

    // --- 409 Conflict ---
    CONFLICT(HttpStatus.CONFLICT, "COMMON-409", "충돌이 발생했습니다."),

    // --- 413 Payload Too Large ---
    PAYLOAD_TOO_LARGE(HttpStatus.PAYLOAD_TOO_LARGE, "COMMON-413", "요청 크기가 너무 큽니다."),

    // --- 415 Unsupported Media Type ---
    UNSUPPORTED_MEDIA_TYPE(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "COMMON-415", "지원하지 않는 미디어 타입입니다."),

    // --- 500 Internal Server Error ---
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON-500", "서버 내부 오류입니다."),

    // --- 503 Service Unavailable ---
    SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "COMMON-503", "일시적으로 서비스를 이용할 수 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    CommonErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

    @Override public HttpStatus status() { return status; }
    @Override public String code() { return code; }
    @Override public String message() { return message; }
}
