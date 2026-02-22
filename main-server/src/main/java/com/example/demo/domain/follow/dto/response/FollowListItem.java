package com.example.demo.domain.follow.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "팔로워 목록 응답 아이템")
public class FollowListItem {

    @Schema(description = "팔로우 ID", example = "1")
    private Long followId;

    @Schema(description = "유저 ID", example = "hong123")
    private String userId;

    @Schema(description = "유저 닉네임", example = "홍길동")
    private String username;

    @Schema(description = "공유 코드", example = "ABCD1234")
    private String shareCode;

    @Schema(description = "프로필 이미지 URL", example = "https://cdn.example.com/profile.jpg")
    private String profileUrl;

    @Schema(description = "현재 로그인한 사용자가 이 유저를 팔로우 중인지 여부", example = "true")
    private boolean followedByMe;

    public void changeFollowedByMe(boolean followedByMe) {
        this.followedByMe = followedByMe;
    }

    public FollowListItem(
            Long followId,
            String userId,
            String username,
            String shareCode,
            String profileUrl
    ) {
        this.followId = followId;
        this.userId = userId;
        this.username = username;
        this.shareCode = shareCode;
        this.profileUrl = profileUrl;
    }
}
