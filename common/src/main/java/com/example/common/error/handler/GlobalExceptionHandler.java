package com.example.common.error.handler;

import com.example.common.error.code.CommonErrorCode;
import com.example.common.error.code.ErrorCode;
import com.example.common.error.code.ErrorResponse;
import com.example.common.error.exception.BaseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GlobalExceptionHandler {

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ErrorResponse> handleBase(BaseException e, HttpServletRequest req) {
        ErrorCode ec = e.getErrorCode();
        ErrorResponse body = ErrorResponse.of(
                ec,
                e.getMessage(),
                req.getRequestURI(),
                req.getMethod(),
                e.getDetails()
        );
        return ResponseEntity.status(ec.status()).body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException e, HttpServletRequest req) {
        Map<String, Object> details = new HashMap<>();
        e.getBindingResult().getFieldErrors().forEach(fe -> {
            details.put(fe.getField(), fe.getDefaultMessage());
        });

        ErrorResponse body = ErrorResponse.of(
                CommonErrorCode.VALIDATION_FAILED,
                "요청 필드 검증에 실패했습니다.",
                req.getRequestURI(),
                req.getMethod(),
                details
        );
        return ResponseEntity.status(CommonErrorCode.VALIDATION_FAILED.status()).body(body);
    }

    @ExceptionHandler({ MethodArgumentTypeMismatchException.class, HttpMessageNotReadableException.class })
    public ResponseEntity<ErrorResponse> handleBadRequest(Exception e, HttpServletRequest req) {
        ErrorResponse body = ErrorResponse.of(
                CommonErrorCode.BAD_REQUEST,
                e.getMessage(),
                req.getRequestURI(),
                req.getMethod(),
                Map.of()
        );
        return ResponseEntity.status(CommonErrorCode.BAD_REQUEST.status()).body(body);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParam(MissingServletRequestParameterException e, HttpServletRequest req) {
        ErrorResponse body = ErrorResponse.of(
                CommonErrorCode.BAD_REQUEST,
                "필수 요청 파라미터가 누락되었습니다.",
                req.getRequestURI(),
                req.getMethod(),
                Map.of("parameter", e.getParameterName())
        );
        return ResponseEntity.status(CommonErrorCode.BAD_REQUEST.status()).body(body);
    }

    @ExceptionHandler(ServletRequestBindingException.class)
    public ResponseEntity<ErrorResponse> handleBinding(ServletRequestBindingException e, HttpServletRequest req) {
        ErrorResponse body = ErrorResponse.of(
                CommonErrorCode.BAD_REQUEST,
                "요청 바인딩에 실패했습니다.",
                req.getRequestURI(),
                req.getMethod(),
                Map.of()
        );
        return ResponseEntity.status(CommonErrorCode.BAD_REQUEST.status()).body(body);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException e, HttpServletRequest req) {
        Map<String, Object> details = new HashMap<>();
        e.getConstraintViolations().forEach(v -> {
            String field = v.getPropertyPath().toString();
            details.put(field, v.getMessage());
        });

        ErrorResponse body = ErrorResponse.of(
                CommonErrorCode.VALIDATION_FAILED,
                "요청 파라미터 검증에 실패했습니다.",
                req.getRequestURI(),
                req.getMethod(),
                details
        );
        return ResponseEntity.status(CommonErrorCode.VALIDATION_FAILED.status()).body(body);
    }

    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<ErrorResponse> handleMultipart(MultipartException e, HttpServletRequest req) {
        ErrorResponse body = ErrorResponse.of(
                CommonErrorCode.PAYLOAD_TOO_LARGE,
                "파일 업로드 중 오류가 발생했습니다.",
                req.getRequestURI(),
                req.getMethod(),
                Map.of()
        );
        return ResponseEntity.status(CommonErrorCode.PAYLOAD_TOO_LARGE.status()).body(body);
    }

    @ExceptionHandler(JsonMappingException.class)
    public ResponseEntity<ErrorResponse> handleJsonMapping(JsonMappingException e, HttpServletRequest req) {
        ErrorResponse body = ErrorResponse.of(
                CommonErrorCode.BAD_REQUEST,
                "JSON 필드 매핑에 실패했습니다.",
                req.getRequestURI(),
                req.getMethod(),
                Map.of()
        );
        return ResponseEntity.status(CommonErrorCode.BAD_REQUEST.status()).body(body);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NoHandlerFoundException e, HttpServletRequest req) {
        ErrorResponse body = ErrorResponse.of(
                CommonErrorCode.NOT_FOUND,
                "요청 경로를 찾을 수 없습니다.",
                req.getRequestURI(),
                req.getMethod(),
                Map.of("path", e.getRequestURL())
        );
        return ResponseEntity.status(CommonErrorCode.NOT_FOUND.status()).body(body);
    }

    @ExceptionHandler(ErrorResponseException.class)
    public ResponseEntity<ErrorResponse> handleErrorResponse(ErrorResponseException e, HttpServletRequest req) {
        String reason = e.getMessage();
        String message = reason != null && !reason.isBlank() ? reason : CommonErrorCode.INTERNAL_ERROR.message();

        ErrorResponse body = new ErrorResponse(
                LocalDateTime.now(),
                e.getStatusCode().value(),
                CommonErrorCode.INTERNAL_ERROR.code(),
                message,
                req.getRequestURI(),
                req.getMethod(),
                Map.of()
        );
        return ResponseEntity.status(e.getStatusCode()).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAny(Exception e, HttpServletRequest req) {
        ErrorResponse body = ErrorResponse.of(
                CommonErrorCode.INTERNAL_ERROR,
                "예상하지 못한 오류가 발생했습니다.",
                req.getRequestURI(),
                req.getMethod(),
                Map.of("error", e.getClass().getSimpleName())
        );
        return ResponseEntity.status(CommonErrorCode.INTERNAL_ERROR.status()).body(body);
    }
}