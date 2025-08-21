package com.example.demo.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChatOutbound {
    private String roomId;
    private String messageId;
    private String senderId;
    private String content;
    private String sentAt;
    private boolean systemMessage;
}
