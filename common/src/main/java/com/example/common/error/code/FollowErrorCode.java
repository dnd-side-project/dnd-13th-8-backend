package com.example.common.error.code;
import org.springframework.http.HttpStatus;

public enum FollowErrorCode implements ErrorCode {

    NOT_REPRESENTATIVE_PLAYLIST(HttpStatus.BAD_REQUEST, "FOLLOW-400", "대표 플레이리스트만 팔로우할 수 있습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    FollowErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

    @Override public HttpStatus status() { return status; }
    @Override public String code() { return code; }
    @Override public String message() { return message; }
}
