package com.example.common.error.exception;

import com.example.common.error.code.ErrorCode;

public class CdException extends BaseException {
    public CdException(String mea, ErrorCode code) {
        super(mea, code);
    }
}
