package com.example.demo.dto.chat;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChatOutbound {
    private String roomId;
    private String messageId;
    private String senderId;
    private String username;
    private String content;
    private String sentAt;
    private String profileImage;
    private boolean systemMessage;
}
