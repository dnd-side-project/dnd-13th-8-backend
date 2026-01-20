package com.example.demo.dto;

import lombok.Builder;

@Builder
public record ChatUserProfile(String userId, String username, String profileImage) {
}
