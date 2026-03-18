package com.example.demo.global.discord.dto;

public record DiscordWebhookRequest(
        String content
) {
    public static DiscordWebhookRequest of(String content) {
        return new DiscordWebhookRequest(content);
    }
}
