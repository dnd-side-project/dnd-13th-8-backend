package com.example.demo.domain.song.dto;

public record YouTubeVideoInfoDto(
        String link,          // 사용자가 입력한 유튜브 링크
        String title,         // 영상 제목
        String thumbnailUrl,  // 썸네일 URL (high)
        String duration       // 영상 길이 (예: "03:22")
) {}
