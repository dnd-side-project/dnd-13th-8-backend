package com.example.demo.kakao.dto;

import com.example.common.error.code.KakaoErrorCode;
import com.example.common.error.exception.KakoException;
import com.example.demo.domain.user.entity.Users;
import com.example.demo.domain.user.service.NicknameGenerator;
import com.example.demo.global.jwt.JwtRoleType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
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

    public String profileImageFrom(KakaoProfileResponse profileResponse) {
        if (profileResponse != null &&
                profileResponse.kakao_account() != null &&
                profileResponse.kakao_account().profile() != null) {

            String imageUrl = profileResponse.kakao_account().profile().profileImageUrl();
            if (imageUrl != null && !imageUrl.isBlank()) {
                log.info("카카오 프로필 이미지 URL: {}", imageUrl);
                return imageUrl;
            } else {
                log.warn("프로필 이미지 URL이 null 또는 blank입니다.");
            }
        } else {
            log.warn("카카오 프로필 응답이 null이거나 하위 필드가 존재하지 않습니다.");
        }

        // 없으면 기본 이미지 경로로 대체
        return "NULL";
    }


    public Users newUserFromProfile(KakaoProfileResponse p) {
        String kakaoId = kakaoIdFrom(p);
        String nickname = nicknameFrom(p);
        String profileImageUrl = profileImageFrom(p);

        return Users.builder()
                .kakaoId(kakaoId)
                .username(nickname)
                .role(JwtRoleType.USER)
                .profileUrl(profileImageUrl)
                .enabled(true)
                .build();
    }
}
