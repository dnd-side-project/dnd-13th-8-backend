package com.example.common.error.exception;

import com.example.common.error.code.ErrorCode;

public class KakoException extends DomainException {

    public KakoException(String message, ErrorCode code) {
        super(message, code);
    }

    public KakoException(ErrorCode code) {
        super(code);
    }
}
