package com.example.demo.domain.follow.dto.response;

import lombok.Getter;

@Getter
public class FollowListItem {
    private Long followId;
    private String userId;
    private String username;
    private String shareCode;
    private String profileUrl;
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
