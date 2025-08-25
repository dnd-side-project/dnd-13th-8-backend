package com.example.demo.kakao.dto;


import com.example.common.error.code.KakaoErrorCode;
import com.example.common.error.exception.KakoException;
import com.example.demo.domain.user.entity.Users;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.stereotype.Component;

@Component
public class KakaoProfileMapper {

    private static final List<String> MUSIC_PREFIXES = List.of(
            "박자도둑", "리듬타는곰", "멜로디요정", "하모니마스터", "코드마술사",
            "비트장인", "음표수집가", "소리여행자", "락앤롤영웅", "발라드감성",
            "재즈고양이", "힙합토끼", "클래식덕후", "댄스킹", "노래방1등",
            "베이스폭격기", "드럼캣", "기타소년", "피아노천사", "보컬지존",
            "DJ바나나", "헤드셋러버", "이어폰귀신", "마이크잡이", "노래하는펭귄",
            "작곡꿈나무", "가사시인", "음색깡패", "트로트장군", "소울파이터",
            "떼창요정", "버스킹소년", "코러스여신", "락스피릿", "아이돌연습생",
            "음악중독자", "노래쟁이", "코드부자", "사운드메이커", "리듬전사"
    );

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


    public String nicknameFrom(KakaoProfileResponse profileResponse) {
        String nick = null;

        if (profileResponse != null
                && profileResponse.kakao_account() != null
                && profileResponse.kakao_account().profile() != null) {
            nick = profileResponse.kakao_account().profile().nickname();
        }

        if (nick == null || nick.isBlank()) {
            String randomPrefix = MUSIC_PREFIXES.get(
                    ThreadLocalRandom.current().nextInt(MUSIC_PREFIXES.size())
            );

            if (profileResponse == null || profileResponse.id() == null) {
                return randomPrefix + "-들락";
            }

            // id 앞 4자리만 사용
            String shortId = String.valueOf(profileResponse.id());
            if (shortId.length() > 4) {
                shortId = shortId.substring(0, 4);
            }

            return randomPrefix + "-들락-" + shortId;
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
