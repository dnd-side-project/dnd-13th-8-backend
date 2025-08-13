package com.example.common.error.exception;

import com.example.common.error.code.ErrorCode;

/** User 도메인 루트 예외 */
public class UserException extends DomainException {
    public UserException(String message, ErrorCode code) {
        super(message, code);
    }

    public UserException(ErrorCode code) {
        super(code);
    }
}

