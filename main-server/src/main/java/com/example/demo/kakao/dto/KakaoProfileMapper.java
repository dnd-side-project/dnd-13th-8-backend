package com.example.demo.kakao.dto;

import com.example.common.error.code.KakaoErrorCode;
import com.example.common.error.exception.KakoException;
import com.example.demo.domain.user.entity.Users;
import com.example.demo.domain.user.service.NicknameGenerator;
import com.example.demo.global.jwt.JwtRoleType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KakaoProfileMapper {

    private final NicknameGenerator nicknameGenerator;

    public String kakaoIdFrom(KakaoProfileResponse profileResponse) {
        if (profileResponse == null || profileResponse.id() == null) {
            throw new KakoException("카카오 프로필의 id가 비어 있습니다.", KakaoErrorCode.KAKAO_BAD_REQUEST);
        }
        return String.valueOf(profileResponse.id());
    }

    public String nicknameFrom(KakaoProfileResponse profileResponse) {
        if (profileResponse != null &&
                profileResponse.kakao_account() != null &&
                profileResponse.kakao_account().profile() != null) {

            String kakaoNick = profileResponse.kakao_account().profile().nickname();
            if (kakaoNick != null && !kakaoNick.isBlank()) {
                return kakaoNick;
            }
        }

        return nicknameGenerator.generateUniqueNickname();
    }

    public Users newUserFromProfile(KakaoProfileResponse p) {
        String kakaoId = kakaoIdFrom(p);
        String nickname = nicknameFrom(p);

        return Users.builder()
                .kakaoId(kakaoId)
                .username(nickname)
                .role(JwtRoleType.USER)
                .enabled(true)
                .build();
    }
}
