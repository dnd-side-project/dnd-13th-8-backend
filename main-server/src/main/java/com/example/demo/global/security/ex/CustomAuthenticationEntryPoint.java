package com.example.demo.global.security.ex;

import com.example.common.error.code.CommonErrorCode;
import com.example.common.error.code.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        if (request.getRequestURI().equals("/error")) {
            return;
        }

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json;charset=UTF-8");

        ErrorResponse body = ErrorResponse.of(
                CommonErrorCode.UNAUTHORIZED,
                "로그인이 필요한 요청입니다.",
                request.getRequestURI(),
                request.getMethod(),
                Map.of()
        );

        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
