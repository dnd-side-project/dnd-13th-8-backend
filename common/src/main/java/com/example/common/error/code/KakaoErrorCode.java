package com.example.common.error.code;

import org.springframework.http.HttpStatus;

public enum KakaoErrorCode implements ErrorCode {
    KAKAO_AUTH_FAILED(HttpStatus.UNAUTHORIZED, "KAKAO-401-AUTH", "카카오 인증에 실패했습니다."),
    KAKAO_BAD_REQUEST(HttpStatus.BAD_REQUEST, "KAKAO-400", "카카오 요청 파라미터가 올바르지 않습니다."),
    KAKAO_API_ERROR(HttpStatus.BAD_GATEWAY, "KAKAO-502", "카카오 API 처리 중 오류가 발생했습니다."),
    KAKAO_RATE_LIMIT(HttpStatus.TOO_MANY_REQUESTS, "KAKAO-429", "카카오 호출 한도를 초과했습니다."),
    KAKAO_TIMEOUT(HttpStatus.GATEWAY_TIMEOUT, "KAKAO-504", "카카오 응답 시간이 초과되었습니다."),
    KAKAO_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "KAKAO-503", "카카오 서비스가 일시적으로 사용 불가입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    KakaoErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

    @Override public HttpStatus status() { return status; }
    @Override public String code() { return code; }
    @Override public String message() { return message; }
}
