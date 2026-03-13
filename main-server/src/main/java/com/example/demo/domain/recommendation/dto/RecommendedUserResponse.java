package com.example.demo.domain.recommendation.dto;

public record RecommendedUserResponse(String userId,
                                      String nickname,
                                      String profileUrl,
                                      String shareCode) {
}
