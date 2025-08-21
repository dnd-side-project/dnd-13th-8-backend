package com.example.demo.global.http.util;

import com.example.demo.global.http.dto.CookieProps;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class CookieBuilder {

    private final CookieProps props;

    public CookieBuilder(CookieProps props) {
        if (props == null) {
            throw new IllegalArgumentException("쿠키 설정이 존재하지 않습니다.");
        }
        this.props = props;
    }

    /**
     * 쿠키 생성
     */
    public ResponseCookie build(String sessionId, String value, Duration maxAge, String path) {
        validateName(sessionId);

        ResponseCookie.ResponseCookieBuilder b = ResponseCookie.from(sessionId, value);

        b.httpOnly(props.common().httpOnly());
        b.secure(effectiveSecure());
        b.path(safePath(path));

        if (hasText(props.common().domain())) {
            b.domain(props.common().domain());
        }
        if (hasText(props.common().sameSite())) {
            b.sameSite(props.common().sameSite());
        }
        if (maxAge != null) {
            b.maxAge(maxAge);
        }

        return b.build();
    }

    /**
     * 쿠키 만료 (즉시 삭제)
     */
    public ResponseCookie expire(String sessionId, String path) {
        return build(sessionId,"", Duration.ZERO, path);
    }

    /* ===== 내부 유틸 메서드 ===== */

    private void validateName(String name) {
        if (!hasText(name)) {
            throw new IllegalArgumentException("쿠키 이름이 비어 있습니다.");
        }
    }

    private boolean effectiveSecure() {
        if ("None".equalsIgnoreCase(props.common().sameSite())) {
            return true; // SameSite=None이면 Secure 필수
        }
        return props.common().secure();
    }

    private String safePath(String path) {
        if (hasText(path)) {
            return path;
        } else {
            return "/";
        }
    }


    private boolean hasText(String v) {
        return v != null && !v.isBlank();
    }
}
