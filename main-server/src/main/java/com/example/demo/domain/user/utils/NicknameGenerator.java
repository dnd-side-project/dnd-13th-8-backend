package com.example.demo.domain.user.utils;

import com.example.demo.domain.user.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Random;

@RequiredArgsConstructor
@Component
public class NicknameGenerator {

    private static final List<String> MUSIC_PREFIXES = List.of(
            "비트박스하는", "춤추는", "악기부는", "기타치는", "디제잉하는",
            "연주하는", "랩하는", "노래하는", "코드치는", "리듬타는",
            "작곡하는", "쿵짝이는", "빛나는"
    );

    private static final List<String> SPACE_SUFFIXES = List.of(
            "블랙홀", "토성", "목성", "달", "별", "성운",
            "지구", "별똥별", "은하", "외계인", "금성", "수성", "화성"
    );

    private final UsersRepository usersRepository;
    private final Random random = new Random();

    public  String generateUniqueNickname() {
        String nickname;

        do {
            nickname = generateRandomNickname();
        } while (usersRepository.existsByUsername(nickname));

        return nickname;
    }

    private String generateRandomNickname() {
        String prefix = MUSIC_PREFIXES.get(random.nextInt(MUSIC_PREFIXES.size()));
        String suffix = SPACE_SUFFIXES.get(random.nextInt(SPACE_SUFFIXES.size()));
        String number = String.format("%04d", random.nextInt(10000)); // 0000 ~ 9999

        return prefix + "-" + suffix + "-" + number;
    }
}
