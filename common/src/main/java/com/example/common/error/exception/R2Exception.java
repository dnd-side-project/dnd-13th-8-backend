package com.example.common.error.exception;

import com.example.common.error.ErrorCode;
import com.example.common.error.ExceptionBase;

public class R2Exception extends ExceptionBase {
    public R2Exception(String message) {
        super(message, ErrorCode.R2_ERROR);
    }
}
