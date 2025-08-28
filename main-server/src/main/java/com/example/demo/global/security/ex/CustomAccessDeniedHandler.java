package com.example.demo.global.security.ex;

import com.example.common.error.code.CommonErrorCode;
import com.example.common.error.code.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       org.springframework.security.access.AccessDeniedException accessDeniedException)
            throws IOException, ServletException {

        response.setStatus(HttpStatus.FORBIDDEN.value()); // 403
        response.setContentType("application/json;charset=UTF-8");

        ErrorResponse body = ErrorResponse.of(
                CommonErrorCode.FORBIDDEN,
                "접근 권한이 없습니다.",
                request.getRequestURI(),
                request.getMethod(),
                Map.of()
        );

        response.getWriter().write(objectMapper.writeValueAsString(body));

    }
}
