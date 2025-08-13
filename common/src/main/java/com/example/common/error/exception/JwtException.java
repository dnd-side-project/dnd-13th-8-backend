package com.example.common.error.exception;

import com.example.common.error.code.ErrorCode;

public class JwtException extends DomainException {

    public JwtException(String message, ErrorCode code) {
        super(message, code);
    }

    public JwtException(ErrorCode code) {
        super(code);
    }
}
