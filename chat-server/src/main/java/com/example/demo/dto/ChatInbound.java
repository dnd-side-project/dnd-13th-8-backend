package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ChatInbound {
    @NotBlank
    private String senderId;

    @NotBlank
    private String username;

    @NotBlank
    private String content;

    private boolean systemMessage = false;
}