package com.example.common.error.exception;

import com.example.common.error.code.ErrorCode;

public class PlaylistException extends DomainException {
    public PlaylistException(String message, ErrorCode code) {
        super(message, code);
    }

    public PlaylistException(ErrorCode code) {
        super(code);
    }
}
