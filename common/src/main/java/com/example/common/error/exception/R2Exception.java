package com.example.common.error.exception;

import com.example.common.error.code.ErrorCode;

public class R2Exception extends DomainException {
    public R2Exception(String message, ErrorCode code) {
        super(message, code);
    }

    public R2Exception(ErrorCode code) {
        super(code);
    }
}
