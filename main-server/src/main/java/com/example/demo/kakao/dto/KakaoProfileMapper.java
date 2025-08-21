package com.example.demo.kakao.dto;


import com.example.common.error.code.KakaoErrorCode;
import com.example.common.error.exception.KakoException;
import com.example.demo.domain.user.entity.Users;
import org.springframework.stereotype.Component;

@Component
public class KakaoProfileMapper {

    /** kakao 프로필 → 내부 userId 규칙 */
    public String kakaoIdFrom(KakaoProfileResponse profileResponse) {
        if (profileResponse == null) {
            throw new KakoException(KakaoErrorCode.KAKAO_BAD_REQUEST);
        }
        if (profileResponse.id() == null) {
            throw new KakoException("카카오 프로필의 id가 비어 있습니다.",KakaoErrorCode.KAKAO_BAD_REQUEST);
        }
        return String.valueOf(profileResponse.id());
    }

    /** kakao 프로필 → 닉네임(없으면 kakao-<id>) */
    public String nicknameFrom(KakaoProfileResponse profileResponse) {
        String nick = null;
        if (profileResponse != null) {
            if (profileResponse.kakao_account() != null && profileResponse.kakao_account().profile() != null) {
                nick = profileResponse.kakao_account().profile().nickname();
            }
        }
        if (nick == null || nick.isBlank()) {
            if (profileResponse == null || profileResponse.id() == null) {
                return "kakao-unknown";
            }
            return "kakao-" + profileResponse.id();
        }
        return nick;
    }

    /** 새 User 엔티티 생성(Builder) */
    public Users newUserFromProfile(KakaoProfileResponse p) {
        String kakaoId = kakaoIdFrom(p);
        String nickname = nicknameFrom(p);
        return Users.builder()
                .kakaoId(kakaoId)
                .username(nickname)
                .enabled(true)
                .build();
    }

}
