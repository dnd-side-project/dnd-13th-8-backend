package com.example.common.error.exception;

import com.example.common.error.ErrorCode;
import com.example.common.error.ExceptionBase;

public class NotFoundException extends ExceptionBase {
    public NotFoundException(String message) {
        super(message, ErrorCode.NOT_FOUND);
    }
}