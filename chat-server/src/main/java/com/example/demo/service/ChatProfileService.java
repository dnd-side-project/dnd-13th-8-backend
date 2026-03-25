package com.example.demo.service;

import com.example.common.error.code.UserErrorCode;
import com.example.common.error.exception.UserException;
import com.example.demo.dto.ChatUserProfile;
import com.example.demo.entity.Users;
import com.example.demo.entity.repository.UsersRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatProfileService {

    private final StringRedisTemplate stringRedisTemplate;
    private final UsersRepository usersRepository;
    private final ObjectMapper objectMapper;

    private static final Duration TTL = Duration.ofMinutes(10);

    public ChatUserProfile getOrLoad(String userId) {
        String key = "chat:user:" + userId + ":profile";

        String cached = stringRedisTemplate.opsForValue().get(key);
        if (cached != null && !cached.isBlank()) {
            try {
                return objectMapper.readValue(cached, ChatUserProfile.class);
            } catch (Exception ignore) {
                stringRedisTemplate.delete(key);
            }
        }

        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

        ChatUserProfile profile = ChatUserProfile.from(user);

        try {
            stringRedisTemplate.opsForValue().set(
                    key,
                    objectMapper.writeValueAsString(profile),
                    TTL
            );
        } catch (Exception ignore) {
        }

        return profile;
    }

    public Map<String, ChatUserProfile> getProfiles(Set<String> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyMap();
        }

        List<Users> users = usersRepository.findAllById(userIds);

        return users.stream()
                .collect(Collectors.toMap(
                        Users::getId,
                        ChatUserProfile::from
                ));
    }
}
