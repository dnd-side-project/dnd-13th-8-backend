package com.example.common.discord.dto;

public record DiscordWebhookRequest(
        String content
) {
    public static DiscordWebhookRequest of(String content) {
        return new DiscordWebhookRequest(content);
    }
}
