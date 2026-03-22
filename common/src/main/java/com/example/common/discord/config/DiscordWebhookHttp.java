package com.example.common.discord.config;

import com.example.common.discord.dto.DiscordWebhookRequest;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@HttpExchange(contentType = MediaType.APPLICATION_JSON_VALUE)
public interface DiscordWebhookHttp {

    @PostExchange
    void sendMessage(@RequestBody DiscordWebhookRequest request);
}