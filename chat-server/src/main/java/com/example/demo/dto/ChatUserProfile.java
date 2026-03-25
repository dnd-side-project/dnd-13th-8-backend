package com.example.demo.dto;

import com.example.demo.entity.Users;
import lombok.Builder;

@Builder
public record ChatUserProfile(String userId, String username, String profileImage, String shareCode) {

    public static ChatUserProfile from(Users user) {
        return ChatUserProfile.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .profileImage(user.getProfileUrl())
                .shareCode(user.getShareCode())
                .build();
    }
}
