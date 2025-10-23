package com.example.demo.domain.user.service;

import com.example.common.error.code.UserErrorCode;
import com.example.common.error.exception.UserException;
import com.example.demo.domain.user.dto.UpdateProfileRequest;
import com.example.demo.domain.user.dto.UpdateProfileResponse;
import com.example.demo.domain.user.entity.Users;
import com.example.demo.domain.user.repository.UsersRepository;
import com.example.demo.global.r2.R2Service;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UsersService {

    private final UsersRepository usersRepository;
    private final R2Service r2Service;

    public Users findUserByShareCode(String shareCode) {
        return usersRepository.findUsersByShareCode(shareCode)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));
    }

    @Transactional
    public UpdateProfileResponse updateProfile(String userId, UpdateProfileRequest req)
            throws IOException {

        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

        // 닉네임 업데이트
        if (req.nickname() != null && !req.nickname().isBlank()) {
            user.changeNickname(req.nickname());
        }

        // 프로필 이미지 업데이트
        if (req.profileImage() != null && !req.profileImage().isEmpty()) {

            //가존 이미지가 R2에 저장되어있다면 R2에서 삭제
            String oldImageKey = r2Service.extractKey(user.getProfileUrl());
            if (oldImageKey != null && !oldImageKey.isBlank()) {
                r2Service.delete(oldImageKey);
            }

            String key = r2Service.newKey(req.profileImage().getOriginalFilename());
            r2Service.upload(
                    req.profileImage().getBytes(),
                    req.profileImage().getContentType(),
                    key
            );
            String profileUrl = r2Service.getPublicUrl(key);
            user.changeProfileImage(profileUrl);
        }

        return new UpdateProfileResponse(user.getId(), user.getUsername(), user.getProfileUrl());
    }

    @Transactional
    public void deleteAccount(String userId) {
        if (!usersRepository.existsById(userId)) return;
        usersRepository.deleteById(userId);
    }
}
