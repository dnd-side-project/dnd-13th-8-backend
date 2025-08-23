package com.example.demo.domain.browse.service;

import com.example.demo.domain.user.repository.UsersRepository;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class BrowsePlaylistShuffleService {

    private final StringRedisTemplate redisTemplate;
    private final UsersRepository usersRepository;

    private static final int MAX_ID = 30;

    public void shuffleAndStore(String userId) {
        List<String> ids = IntStream.rangeClosed(1, MAX_ID)
                .mapToObj(String::valueOf)
                .collect(Collectors.toList());

        Collections.shuffle(ids);

        String key = "BROWSE_SHUFFLED:" + userId;
        redisTemplate.delete(key);
        redisTemplate.opsForList().rightPushAll(key, ids);
        redisTemplate.expire(key, Duration.ofHours(24));
    }
    @Scheduled(cron = "0 0 3 * * *", zone = "Asia/Seoul")
    public void scheduledShuffle() {
        List<String> userIds = usersRepository.findAllUserIds(); // 모든 유저 ID 가져오기
        for (String userId : userIds) {
            shuffleAndStore(userId);
        }

        log.info("새벽 3시에 셔플 실행됨");
    }
}
