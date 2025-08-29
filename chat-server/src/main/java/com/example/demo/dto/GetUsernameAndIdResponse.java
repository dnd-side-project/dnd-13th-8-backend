package com.example.demo.dto;

import lombok.Builder;

@Builder
public record GetUsernameAndIdResponse(String userId, String username) {
}
