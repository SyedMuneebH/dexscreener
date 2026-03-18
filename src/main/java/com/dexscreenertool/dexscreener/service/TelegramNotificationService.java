package com.dexscreenertool.dexscreener.service;

import org.springframework.stereotype.Service;

@Service
public class TelegramNotificationService {

    // TODO: store your bot token and chat ID as fields (load from application.properties via @Value)

    private static final String TELEGRAM_BASE_URL = "https://api.telegram.org/bot";

    /**
     * Sends a text message to your Telegram chat via the Bot API.
     *
     * Hints:
     * 1. Build the URL: TELEGRAM_BASE_URL + botToken + "/sendMessage"
     * 2. URL-encode the message text (URLEncoder.encode(message, StandardCharsets.UTF_8))
     * 3. Append query params: ?chat_id=...&text=...
     * 4. Use HttpClient to fire a GET request to that URL
     */
    public void send(String message) {
        // TODO: implement
    }
}
