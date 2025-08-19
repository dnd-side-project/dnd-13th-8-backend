package com.example.common.error.exception;

import com.example.common.error.code.ErrorCode;

public class PropException extends DomainException {
    public PropException(String message, ErrorCode code) {
        super(message, code);
    }

    public PropException(ErrorCode code) {
        super(code);
    }
}
