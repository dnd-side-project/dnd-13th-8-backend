package com.example.demo.global.jwt;

import com.example.common.error.code.JwtErrorCode;
import com.example.common.error.exception.BaseException;
import com.example.common.error.exception.JwtException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

public class JwtProvider {

    private final JwtProps props;
    private final SecretKey key;

    public JwtProvider(JwtProps props) {
        if (props == null) {
            throw new JwtException("JWT 설정이 존재하지 않습니다.", JwtErrorCode.JWT_INVALID);
        }
        this.props = props;

        if (props.secretBase64() == null || props.secretBase64().isBlank()) {
            throw new JwtException("JWT 시크릿이 비어 있습니다.", JwtErrorCode.JWT_INVALID);
        }
        byte[] secret = Decoders.BASE64.decode(props.secretBase64());
        if (secret == null || secret.length < 32) { // 256비트 미만 금지
            throw new JwtException("JWT 시크릿 길이가 충분하지 않습니다. 256비트 이상이어야 합니다.", JwtErrorCode.JWT_INVALID);
        }
        this.key = Keys.hmacShaKeyFor(secret);

        if (props.issuer() == null || props.issuer().isBlank()) {
            throw new JwtException("JWT issuer가 비어 있습니다.", JwtErrorCode.JWT_INVALID);
        }
    }


    /** Access 토큰 검증(서명/만료 + typ=access) */
    public Jws<Claims> validateAccess(String jwt) {
        Jws<Claims> jws = parse(jwt);
        String typ = jws.getPayload().get("typ", String.class);
        if (typ == null || !typ.equals("access")) {
            throw new JwtException("토큰 유형이 access가 아닙니다.", JwtErrorCode.JWT_TYP_MISMATCH);
        }
        return jws;
    }

    /** Refresh 토큰 검증(서명/만료 + typ=refresh) */
    public Jws<Claims> validateRefresh(String jwt) {
        Jws<Claims> jws = parse(jwt);
        String typ = jws.getPayload().get("typ", String.class);
        if (typ == null || !typ.equals("refresh")) {
            throw new JwtException("토큰 유형이 refresh가 아닙니다.", JwtErrorCode.JWT_TYP_MISMATCH);
        }
        return jws;
    }

    /** refresh/access 공통 JTI(claims.getId()) 추출 */
    public String jti(String jwt) {
        var jws = parse(jwt);
        String id = jws.getPayload().getId();
        if (id == null || id.isBlank()) {
            throw new JwtException("토큰에 JTI가 없습니다.", JwtErrorCode.JWT_INVALID);
        }
        return id;
    }


    /* ========== 내부 구현 ========== */

    public String buildToken(String userId, long ttlSeconds, Map<String, Object> extraClaims) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(ttlSeconds);

        var builder = Jwts.builder()
                .id(UUID.randomUUID().toString())
                .issuer(props.issuer())
                .subject(userId)
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .claims(extraClaims)
                .signWith(key);

        if (props.audience() != null && !props.audience().isBlank()) {
            builder.audience().add(props.audience()).and();
        }

        return builder.compact();
    }

    /** 공통 파서(서명/만료/스큐 검증) */
    public Jws<Claims> parse(String jwt) {
        if (jwt == null) {
            throw new JwtException("JWT가 비어 있습니다.", JwtErrorCode.JWT_INVALID);
        }
        String token = jwt.trim();
        if (token.isEmpty()) {
            throw new JwtException("JWT가 비어 있습니다.", JwtErrorCode.JWT_INVALID);
        }
        try {
            return Jwts.parser()
                    .verifyWith(key)
                    .requireIssuer(props.issuer())
                    .clockSkewSeconds(props.leewaySeconds())
                    .build()
                    .parseSignedClaims(token);
        } catch (io.jsonwebtoken.ExpiredJwtException ex) {
            throw new JwtException("토큰이 만료되었습니다.", JwtErrorCode.JWT_EXPIRED);
        } catch (io.jsonwebtoken.security.SignatureException ex) {
            throw new JwtException("토큰 서명 검증에 실패했습니다.", JwtErrorCode.JWT_SIGNATURE_INVALID);
        } catch (io.jsonwebtoken.MalformedJwtException ex) {
            throw new JwtException("토큰 형식이 잘못되었습니다.", JwtErrorCode.JWT_MALFORMED);
        } catch (io.jsonwebtoken.UnsupportedJwtException ex) {
            throw new JwtException("지원하지 않는 토큰입니다.", JwtErrorCode.JWT_UNSUPPORTED);
        } catch (Exception ex) {
            throw new JwtException("유효하지 않은 토큰입니다.", JwtErrorCode.JWT_INVALID);
        }
    }

    public void validateSubject(String userId) {
        if (userId == null) {
            throw new JwtException("userId가 null 입니다.", JwtErrorCode.JWT_INVALID);
        }
        if (userId.isBlank()) {
            throw new JwtException("userId가 비어 있습니다.", JwtErrorCode.JWT_INVALID);
        }
    }
}
