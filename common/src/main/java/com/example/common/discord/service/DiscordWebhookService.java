package com.example.common.discord.service;

import com.example.common.discord.config.DiscordWebhookHttp;
import com.example.common.discord.dto.DiscordWebhookRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DiscordWebhookService {

    private final DiscordWebhookHttp discordWebhookHttp;

    public void sendMessage(String message) {
        discordWebhookHttp.sendMessage(DiscordWebhookRequest.of(message));
    }
}