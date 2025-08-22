package com.example.demo.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record ChatHistoryResponseDto(List<ChatOutbound> messages, String nextCursor) {
}
