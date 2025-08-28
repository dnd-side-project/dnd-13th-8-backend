package com.example.common.error.exception;

import com.example.common.error.code.ErrorCode;

public class PlaylistSearchException extends BaseException {
    public PlaylistSearchException(String message, ErrorCode errorCode) {
        super(message, errorCode);
    }
}
