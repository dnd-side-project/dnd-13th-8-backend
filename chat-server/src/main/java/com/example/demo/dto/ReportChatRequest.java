package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;

public record ReportChatRequest(
        @NotBlank
        String playlistName,

        @NotBlank
        String content
) {
}
