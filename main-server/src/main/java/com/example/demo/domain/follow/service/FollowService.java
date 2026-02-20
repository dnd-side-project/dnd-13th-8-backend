package com.example.demo.domain.follow.service;


import com.example.common.error.code.UserErrorCode;
import com.example.common.error.exception.UserException;
import com.example.demo.domain.follow.dto.response.FollowCountResponse;
import com.example.demo.domain.follow.dto.response.FollowListItem;
import com.example.demo.domain.follow.repository.FollowRepository;
import com.example.demo.global.paging.CursorPageConverter;
import com.example.demo.global.paging.CursorPageResponse;
import com.example.demo.domain.user.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FollowService {

    private final UsersRepository usersRepository;
    private final FollowRepository followRepository;

    @Transactional
    public void follow(String followerId, String followeeId) {
        if (!usersRepository.existsById(followerId)) {
            throw new UserException(UserErrorCode.USER_NOT_FOUND);
        }

        if (!usersRepository.existsById(followeeId)) {
            throw new UserException(UserErrorCode.USER_NOT_FOUND);
        }

        followRepository.insertIfNotExists(followerId, followeeId);
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

    public boolean isUserFollowing(String followerId, String followeeId) {
        return followRepository.existsByFollower_IdAndFollowee_Id(followerId, followeeId);
    }

    @Transactional(readOnly = true)
    public CursorPageResponse<FollowListItem, Long> getFollowerList(
            String userId,
            String meId,
            Long cursor,
            int limit
    ) {

        Long effectiveCursor = (cursor == null || cursor <= 0) ? null : cursor;

        List<FollowListItem> followListItemList =
                followRepository.findFollowerListByUserId(userId, effectiveCursor, limit);

        List<String> followerIds = followListItemList.stream()
                .map(FollowListItem::getUserId)
                .toList();

        List<String> myFollowingIds = followRepository.findFolloweeIdsIn(meId, followerIds);

        Set<String> myFollowingIdSet = new HashSet<>(myFollowingIds);

        followListItemList.forEach(item ->
                item.changeFollowedByMe(myFollowingIdSet.contains(item.getUserId()))
        );

        followListItemList.sort(
                Comparator
                        .comparing((FollowListItem item) -> item.getUserId().equals(meId))
                        .reversed()
                        .thenComparing(FollowListItem::isFollowedByMe)
                        .reversed()
        );

        return CursorPageConverter.toCursorResponse(
                followListItemList,
                limit,
                Function.identity(),
                FollowListItem::getFollowId,
                0L
        );
    }

    @Transactional(readOnly = true)
    public CursorPageResponse<FollowListItem, Long> getFolloweeList(
            String userId,
            String meId,
            Long cursor,
            int limit
    ) {
        Long effectiveCursor = (cursor == null || cursor <= 0) ? null : cursor;

        List<FollowListItem> followListItemList =
                followRepository.findFolloweeListByUserId(userId, effectiveCursor, limit);

        List<String> followeeIds = followListItemList.stream()
                .map(FollowListItem::getUserId)
                .toList();

        Set<String> myFollowingIdSet = new HashSet<>(
                followRepository.findFolloweeIdsIn(meId, followeeIds)
        );

        followListItemList.forEach(item ->
                item.changeFollowedByMe(myFollowingIdSet.contains(item.getUserId()))
        );

        followListItemList.sort(
                Comparator
                        .comparing((FollowListItem item) -> item.getUserId().equals(meId))
                        .reversed()
                        .thenComparing(FollowListItem::isFollowedByMe)
                        .reversed()
        );

        return CursorPageConverter.toCursorResponse(
                followListItemList,
                limit,
                Function.identity(),
                FollowListItem::getFollowId,
                0L
        );
    }

    @Transactional(readOnly = true)
    public FollowCountResponse getFollowCount (String userId) {
        long followerCount = followRepository.countFollowerByUsers_Id(userId);
        long followingCount = followRepository.countFolloweeByUsers_Id(userId);

        return new FollowCountResponse(followerCount, followingCount);
    }

}
