package com.example.demo.domain.follow.service;


import com.example.common.error.code.UserErrorCode;
import com.example.common.error.exception.UserException;
import com.example.demo.domain.follow.repository.FollowRepository;
import com.example.demo.domain.user.entity.Users;
import com.example.demo.domain.user.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FollowService {

    private final UsersRepository usersRepository;
    private final FollowRepository followRepository;

    @Transactional
    public void follow(String followerId, String followeeId) {
        Users follower = usersRepository.findById(followerId)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

        Users followee = usersRepository.findById(followeeId)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

        followRepository.insertIfNotExists(follower.getId(), followee.getId());
    }

    @Transactional
    public void unfollow(String followerId, String followeeId) {
        if (!usersRepository.existsById(followerId)) {
            throw new UserException(UserErrorCode.USER_NOT_FOUND);
        }

        if (!usersRepository.existsById(followeeId)) {
            throw new UserException(UserErrorCode.USER_NOT_FOUND);
        }

        followRepository.deleteByFollower_IdAndFollowee_Id(followerId, followeeId);
    }

    @Transactional
    public boolean isUserFollowing(String followerId, String followeeId) {
        return followRepository.existsByFollower_IdAndFollowee_Id(followerId, followeeId);
    }
}
