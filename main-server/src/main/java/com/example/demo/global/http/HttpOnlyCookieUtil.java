package com.example.demo.global.http;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

@Component
public class HttpOnlyCookieUtil {

    private final CookieProps props;

    public HttpOnlyCookieUtil(CookieProps props) {
        if (props == null) {
            throw new IllegalArgumentException("쿠키 설정이 존재하지 않습니다.");
        }
        this.props = props;
        validateSameSiteSecureRule();
    }

    /* ===== 발급 ===== */

    public ResponseCookie accessCookie(String jwt) {
        requireText(jwt, "access 토큰");
        return build(props.accessName(), jwt, Duration.ofMinutes(props.accessTtlMinutes()), props.accessPath());
    }

    /* ===== 삭제(만료) ===== */

    public ResponseCookie clearAccessCookie() {
        return expire(props.accessName(), props.accessPath());
    }

    /* ===== 조회(요청에서 Access 쿠키 꺼내기) ===== */

    public Optional<String> readAccess(HttpServletRequest req) {
        return read(req, props.accessName());
    }

    /* ===== 내부 구현 ===== */

    private ResponseCookie build(String name, String value, Duration maxAge, String path) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("쿠키 이름이 비어 있습니다.");
        }
        ResponseCookie.ResponseCookieBuilder b = ResponseCookie.from(name, value);

        b.httpOnly(props.httpOnly());
        b.secure(effectiveSecure());
        b.path(safePath(path));
        if (props.domain() != null && !props.domain().isBlank()) {
            b.domain(props.domain());
        }
        if (props.sameSite() != null && !props.sameSite().isBlank()) {
            b.sameSite(props.sameSite());
        }
        if (maxAge != null) {
            b.maxAge(maxAge);
        }
        return b.build();
    }

    private ResponseCookie expire(String name, String path) {
        ResponseCookie.ResponseCookieBuilder b = ResponseCookie.from(name, "");
        b.httpOnly(props.httpOnly());
        b.secure(effectiveSecure());
        b.path(safePath(path));
        if (props.domain() != null && !props.domain().isBlank()) {
            b.domain(props.domain());
        }
        if (props.sameSite() != null && !props.sameSite().isBlank()) {
            b.sameSite(props.sameSite());
        }
        b.maxAge(Duration.ZERO); // 즉시 만료
        return b.build();
    }

    private Optional<String> read(HttpServletRequest req, String name) {
        if (req == null) return Optional.empty();
        if (name == null || name.isBlank()) return Optional.empty();
        if (req.getCookies() == null) return Optional.empty();

        for (var c : req.getCookies()) {
            if (c == null) continue;
            if (!name.equals(c.getName())) continue;
            String v = c.getValue();
            if (v == null) return Optional.empty();
            if (v.isBlank()) return Optional.empty();
            return Optional.of(v);
        }
        return Optional.empty();
    }

    private boolean effectiveSecure() {
        if (props.sameSite() != null && props.sameSite().equalsIgnoreCase("None")) {
            return true; // SameSite=None이면 Secure 필수
        }
        return props.secure();
    }

    private String safePath(String path) {
        if (path == null || path.isBlank()) {
            return "/";
        }
        return path;
    }

    private void validateSameSiteSecureRule() {
        if (props.sameSite() != null && props.sameSite().equalsIgnoreCase("None")) {
            if (!props.secure()) {
                // 브라우저가 차단할 가능성. 강제 오류 대신 effectiveSecure()에서 보정.
            }
        }
    }

    private void requireText(String v, String name) {
        if (v == null) {
            throw new IllegalArgumentException(name + " 값이 null 입니다.");
        }
        if (v.isBlank()) {
            throw new IllegalArgumentException(name + " 값이 비어 있습니다.");
        }
    }
}
