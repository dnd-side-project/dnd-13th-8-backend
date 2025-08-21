package com.example.demo.domain.playlist.dto;


public record LikedPlaylistDto(
        int likeCount,
        String creatorId,
        String creatorNickname // 만든 사람 닉네
) {
}

