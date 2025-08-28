package com.example.common.error.exception;

import com.example.common.error.code.ErrorCode;

public class FollowException extends BaseException {
    public FollowException(ErrorCode errorCode) {
        super(errorCode);
    }
}
