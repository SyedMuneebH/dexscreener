package com.dexscreenertool.dexscreener.service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TelegramNotificationService {

    private static final String TELEGRAM_BASE_URL = "https://api.telegram.org/bot";

    @Value("${telegram.bot.token}")
    private String botToken;

    @Value("${telegram.chat.id}")
    private String botChatId;

    private static final HttpClient httpClient = HttpClient.newHttpClient();

    /**
     * Sends a text message to your Telegram chat via the Bot API.
     */
    public void send(String message) {
        String encodedMessage = URLEncoder.encode(message, StandardCharsets.UTF_8);
        String url = TELEGRAM_BASE_URL + botToken + "/sendMessage?chat_id=" + botChatId + "&text=" + encodedMessage;
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
        try {
            httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            log.error("Failed to send Telegram message: {}", e.getMessage(), e);
        }
    }
}