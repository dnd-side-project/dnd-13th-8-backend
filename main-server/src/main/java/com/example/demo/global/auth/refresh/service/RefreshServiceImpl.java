package com.example.demo.global.auth.refresh.service;

import com.example.demo.global.auth.refresh.dto.RefreshFailure;
import com.example.demo.global.auth.refresh.dto.RefreshResult;
import com.example.demo.global.auth.refresh.dto.RefreshSuccess;
import com.example.demo.global.auth.refresh.dto.TokenPair;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class RefreshServiceImpl implements RefreshService {

    private final RefreshValidator validator;
    private final TokenIssuer issuer;
    private final RotationService rotation;

    @Override
    public RefreshResult refresh(String presentedRefreshJwt, String userId, String sessionId) {
        // 1) 검증
        var v = validator.validate(presentedRefreshJwt, userId);
        if (!v.valid()) {
            return new RefreshFailure(RefreshFailure.Reason.INVALID);
        }

        // 2) 사전 발급
        var issued = issuer.preIssue(v.userId());

        // 3) 회전 시도
        long code = rotation.rotate(v.userId(), sessionId, v.presentedJti(), issued.nextJti(), issued.refreshTtlSeconds());

        // 4) 결과 매핑
        if (code == 1L) {
            return new RefreshSuccess(new TokenPair(
                    issued.accessToken(), issued.refreshToken(), issued.refreshTtlSeconds()
            ));
        }
        if (code == 0L) {
            return new RefreshFailure(RefreshFailure.Reason.NOT_FOUND);
        }
//        if (code == -1L) {
//            reuseHandler.onSuspiciousReuse(v.getUserId());
//            return new RefreshFailure(RefreshFailure.Reason.MISMATCH);
//        }
        return new RefreshFailure(RefreshFailure.Reason.ERROR);
    }
}
