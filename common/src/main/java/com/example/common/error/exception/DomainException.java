package com.example.common.error.exception;

import com.example.common.error.code.ErrorCode;


/** 모든 도메인별 예외의 루트 타입 */
public abstract class DomainException extends BaseException {
    protected DomainException(ErrorCode code) {
        super(code);
    }
    protected DomainException(String message, ErrorCode code) {
        super(message, code);
    }
}
