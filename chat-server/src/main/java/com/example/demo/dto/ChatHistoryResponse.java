package com.example.demo.dto;

import com.example.demo.dto.chat.ChatOutbound;
import lombok.Builder;

import java.util.List;

@Builder
public record ChatHistoryResponse(List<ChatOutbound> messages, String nextCursor) {
}
