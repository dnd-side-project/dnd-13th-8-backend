package com.example.demo.domain.user.dto.response;

import com.example.demo.domain.follow.dto.response.FollowCount;
import com.example.demo.domain.user.entity.MusicKeyword;
import com.example.demo.domain.user.entity.Users;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "피드 프로필 응답 DTO")
public record GetFeedProfileResponse(String userId,
                                     String username,
                                     String profileUrl,
                                     String shareCode,
                                     String bio,
                                     List<MusicKeyword> keywords,
                                     FollowCount followCount) {

    public static GetFeedProfileResponse from(
            Users user,
            List<MusicKeyword> keywords,
            FollowCount followCount
    ) {
        return new GetFeedProfileResponse(
                user.getId(),
                user.getUsername(),
                user.getProfileUrl(),
                user.getShareCode(),
                user.getBio(),
                keywords,
                followCount
        );
    }
}
