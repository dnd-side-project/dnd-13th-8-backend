package com.example.common.error.code;

import org.springframework.http.HttpStatus;

public enum PlaylistErrorCode implements ErrorCode {

    PLAYLIST_NOT_FOUND(HttpStatus.NOT_FOUND, "PLAYLIST-404", "해당 플레이리스트를 찾을 수 없습니다."),
    PLAYLIST_DRAFT_ERROR(HttpStatus.CONFLICT, "PLAYLIST-409", "임시 저장 데이터가 없거나 만료되었습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    PlaylistErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

    @Override public HttpStatus status() { return status; }
    @Override public String code() { return code; }
    @Override public String message() { return message; }
}
