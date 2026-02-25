package com.example.demo.domain.user.service;

import com.example.common.error.code.UserErrorCode;
import com.example.common.error.exception.UserException;
import com.example.demo.domain.follow.dto.response.FollowCount;
import com.example.demo.domain.follow.service.FollowService;
import com.example.demo.domain.user.dto.request.UpdateProfileRequest;
import com.example.demo.domain.user.dto.response.GetFeedProfileResponse;
import com.example.demo.domain.user.dto.response.IsFeedOwnerResponse;
import com.example.demo.domain.user.dto.response.UpdateProfileResponse;
import com.example.demo.domain.user.entity.MusicKeyword;
import com.example.demo.domain.user.entity.UserMusicKeyword;
import com.example.demo.domain.user.entity.Users;
import com.example.demo.domain.user.repository.UserMusicKeywordRepository;
import com.example.demo.domain.user.repository.UsersRepository;
import com.example.demo.global.r2.R2Service;
import java.io.IOException;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UsersService {

    private final UsersRepository usersRepository;
    private final UserMusicKeywordRepository userMusicKeywordRepository;
    private final FollowService followService;
    private final R2Service r2Service;

    @Transactional(readOnly = true)
    public GetFeedProfileResponse getFeedProfileByShareCode(String shareCode) {

        Users user = usersRepository.findByShareCode(shareCode)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

        List<MusicKeyword> keywords = userMusicKeywordRepository.findAllKeywordsByUsers_Id(user.getId());

        FollowCount followCount = followService.getFollowCount(user.getId());

        return GetFeedProfileResponse.from(user, keywords, followCount);
    }

    @Transactional(readOnly = true)
    public IsFeedOwnerResponse isUserFeedOwner(String userId, String shareCode) {
        Users user = usersRepository.findByShareCode(shareCode)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

        return new IsFeedOwnerResponse(user.getId().equals(userId));
    }


    @Transactional
    public UpdateProfileResponse updateProfile(String userId, UpdateProfileRequest req)
            throws IOException {

        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

        try {
            // 닉네임 업데이트
            if (req.nickname() != null && !req.nickname().isBlank()) {
                user.changeNickname(req.nickname());
            }

            // 프로필 이미지 업데이트
            if (req.profileImage() != null && !req.profileImage().isEmpty()) {

                String key = r2Service.newKey(req.profileImage().getOriginalFilename());
                r2Service.upload(
                        req.profileImage().getBytes(),
                        req.profileImage().getContentType(),
                        key
                );

                //가존 이미지가 R2에 저장되어있다면 R2에서 삭제
                String oldImageKey = r2Service.extractKey(user.getProfileUrl());
                if (oldImageKey != null && !oldImageKey.isBlank()) {
                    r2Service.delete(oldImageKey);
                }

                String profileUrl = r2Service.getPublicUrl(key);
                user.changeProfileImage(profileUrl);
            }

            if (req.musicKeywords() != null) {
                userMusicKeywordRepository.deleteByUsers_Id(userId);

                List<UserMusicKeyword> userMusicKeywordList = req.musicKeywords().stream()
                        .distinct()
                        .map(keyword -> new UserMusicKeyword(user, keyword))
                        .toList();

                userMusicKeywordRepository.saveAll(userMusicKeywordList);
            }

            if (req.nickname() != null && req.shareCode() != null) {
                user.changeShareCode(req.shareCode());
            }

            if (req.bio() != null) {
                user.changeBio(req.bio());
            }

            return UpdateProfileResponse.from(user);
        }
        catch (DataIntegrityViolationException e) {
            throw new UserException(UserErrorCode.DUPLICATED_SHARECODE);
        }

    }

    @Transactional
    public void deleteAccount(String userId) {
        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

        String imageKey = r2Service.extractKey(user.getProfileUrl());
        if (imageKey != null && !imageKey.isBlank()) {
            r2Service.delete(imageKey);
        }

        usersRepository.delete(user);
    }
}
