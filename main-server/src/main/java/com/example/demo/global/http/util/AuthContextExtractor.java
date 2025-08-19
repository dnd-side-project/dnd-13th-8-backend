package com.example.demo.global.http.util;

import com.example.demo.global.http.dto.auth.AuthContextResult;
import com.example.demo.global.http.dto.auth.AuthContext;
import com.example.demo.global.http.dto.CookieProps;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthContextExtractor {

    private final CookieReader cookieReader; // 읽기 전용 유틸
    private final CookieProps props;         // 쿠키 이름/설정 소스

    /** 요청에서 refresh, sessionId, userId를 읽어 유효성 검사 후 컨텍스트로 반환 */
    public AuthContextResult extract(HttpServletRequest req) {
        var rtOpt  = cookieReader.read(req, props.refresh().name());
        if (rtOpt.isEmpty()) return AuthContextResult.fail(AuthContextResult.Reason.MISSING_REFRESH);

        var sidOpt = cookieReader.read(req, props.session().sessionName());
        if (sidOpt.isEmpty()) return AuthContextResult.fail(AuthContextResult.Reason.MISSING_SESSION);

        var uidOpt = cookieReader.read(req, props.session().userIdName());
        if (uidOpt.isEmpty()) return AuthContextResult.fail(AuthContextResult.Reason.MISSING_USER);

        return AuthContextResult.ok(new AuthContext(uidOpt.get(), sidOpt.get(), rtOpt.get()));
    }
}
