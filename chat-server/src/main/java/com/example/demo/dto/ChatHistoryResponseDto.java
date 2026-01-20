package com.example.demo.dto;

import com.example.demo.dto.chat.ChatOutbound;
import lombok.Builder;

import java.util.List;

@Builder
public record ChatHistoryResponseDto(List<ChatOutbound> messages, String nextCursor) {
}
