package com.example.demo.domain.follow.service;


import com.example.common.error.code.UserErrorCode;
import com.example.common.error.exception.UserException;
import com.example.demo.domain.follow.dto.request.FollowSortOption;
import com.example.demo.domain.follow.dto.response.FollowCount;
import com.example.demo.domain.follow.dto.response.FollowListItem;
import com.example.demo.domain.follow.repository.FollowRepository;
import com.example.demo.domain.user.entity.Users;
import com.example.demo.global.paging.CursorPageResponse;
import com.example.demo.domain.user.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FollowService {

    private final UsersRepository usersRepository;
    private final FollowRepository followRepository;

    @Transactional
    public void follow(String followerId, String shareCode) {

        if (!usersRepository.existsById(followerId)) {
            throw new UserException(UserErrorCode.USER_NOT_FOUND);
        }

        Users followee = usersRepository.findByShareCode(shareCode)
                .orElseThrow(()-> new UserException(UserErrorCode.USER_NOT_FOUND));

        followRepository.insertIfNotExists(followerId, followee.getId());
    }

    @Transactional
    public void unfollow(String followerId, String shareCode) {
        if (!usersRepository.existsById(followerId)) {
            throw new UserException(UserErrorCode.USER_NOT_FOUND);
        }

        Users followee = usersRepository.findByShareCode(shareCode)
                .orElseThrow(()-> new UserException(UserErrorCode.USER_NOT_FOUND));

        followRepository.deleteByFollower_IdAndFollowee_Id(followerId, followee.getId());
    }

    public boolean isUserFollowing(String followerId, String shareCode) {

        Users followee = usersRepository.findByShareCode(shareCode)
                .orElseThrow(()-> new UserException(UserErrorCode.USER_NOT_FOUND));

        return followRepository.existsByFollower_IdAndFollowee_Id(followerId, followee.getId());
    }

    @Transactional(readOnly = true)
    public CursorPageResponse<FollowListItem, Long> getFollowerList(
            String shareCode,
            String meId,
            Long cursor,
            int limit,
            FollowSortOption sort
    ) {
        Users user = usersRepository.findByShareCode(shareCode)
                .orElseThrow(()-> new UserException(UserErrorCode.USER_NOT_FOUND));

        Long effectiveCursor = (cursor == null || cursor <= 0) ? null : cursor;
        boolean firstPage = (effectiveCursor == null);

        FollowListItem meItem = firstPage
                ? followRepository.findMeInFollowerList(user.getId(), meId).orElse(null)
                : null;

        int fetchLimit = (firstPage && meItem != null) ? (limit - 1) : limit;

        List<FollowListItem> rows = (fetchLimit <= 0)
                ? List.of()
                : followRepository.findFollowerListByUserId(user.getId(), effectiveCursor, fetchLimit, sort, meId);

        boolean hasNext = rows.size() > fetchLimit;
        List<FollowListItem> page = hasNext ? rows.subList(0, fetchLimit) : rows;

        Long nextCursor = hasNext && !page.isEmpty()
                ? page.get(page.size() - 1).getFollowId()
                : null;

        List<String> ids = page.stream().map(FollowListItem::getUserId).toList();
        Set<String> myFollowingIdSet = ids.isEmpty()
                ? Set.of()
                : new HashSet<>(followRepository.findFolloweeIdsIn(meId, ids));

        page.forEach(item -> item.changeFollowedByMe(myFollowingIdSet.contains(item.getUserId())));

        List<FollowListItem> content = new ArrayList<>(limit);
        if (meItem != null) content.add(meItem);
        content.addAll(page);

        long totalCount = followRepository.countFollowerByUsers_Id(user.getId());

        return new CursorPageResponse<>(content, nextCursor, content.size(), hasNext, totalCount);
    }

    @Transactional(readOnly = true)
    public CursorPageResponse<FollowListItem, Long> getFollowingList(
            String shareCode,
            String meId,
            Long cursor,
            int limit,
            FollowSortOption sort
    ) {

        Users user = usersRepository.findByShareCode(shareCode)
                .orElseThrow(()-> new UserException(UserErrorCode.USER_NOT_FOUND));

        Long effectiveCursor = (cursor == null || cursor <= 0) ? null : cursor;
        boolean firstPage = (effectiveCursor == null);

        FollowListItem meItem = firstPage
                ? followRepository.findMeInFolloweeList(user.getId(), meId).orElse(null)
                : null;

        int fetchLimit = (firstPage && meItem != null) ? (limit - 1) : limit;

        List<FollowListItem> rows = (fetchLimit <= 0)
                ? List.of()
                : followRepository.findFolloweeListByUserId(user.getId(), effectiveCursor, fetchLimit, sort, meId);

        boolean hasNext = rows.size() > fetchLimit;
        List<FollowListItem> page = hasNext ? rows.subList(0, fetchLimit) : rows;

        Long nextCursor = hasNext && !page.isEmpty()
                ? page.get(page.size() - 1).getFollowId()
                : null;

        List<String> ids = page.stream().map(FollowListItem::getUserId).toList();
        Set<String> myFollowingIdSet = ids.isEmpty()
                ? Set.of()
                : new HashSet<>(followRepository.findFolloweeIdsIn(meId, ids));

        page.forEach(item -> item.changeFollowedByMe(myFollowingIdSet.contains(item.getUserId())));

        List<FollowListItem> content = new ArrayList<>(limit);
        if (meItem != null) content.add(meItem);
        content.addAll(page);

        long totalCount = followRepository.countFolloweeByUsers_Id(user.getId());

        return new CursorPageResponse<>(
                content,
                nextCursor,
                content.size(),
                hasNext,
                totalCount
        );
    }

    @Transactional(readOnly = true)
    public FollowCount getFollowCount (String userId) {
        long followerCount = followRepository.countFollowerByUsers_Id(userId);
        long followingCount = followRepository.countFolloweeByUsers_Id(userId);

        return new FollowCount(followerCount, followingCount);
    }

}
