package com.dexscreenertool.dexscreener.controller;

import com.dexscreenertool.dexscreener.service.HeliusWalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Receives webhook POST callbacks from Helius when a tracked wallet transacts on Solana.
 *
 * The endpoint URL must be publicly reachable and match the value set in
 * application.properties under helius.webhook.url.
 *
 * Helius posts an array of enhanced transaction objects to this endpoint.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/helius")
public class HeliusWebhookController {

    private final HeliusWalletService heliusWalletService;

    /**
     * Helius will POST to this endpoint whenever a tracked wallet executes a transaction.
     * The Authorization header is validated against helius.auth.header (if configured).
     */
    @PostMapping("/webhook")
    public ResponseEntity<Void> receiveWebhook(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody String body) {

        if (!heliusWalletService.isValidRequest(authHeader)) {
            log.warn("Rejected Helius webhook — invalid Authorization header");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        log.debug("Helius webhook received: {}", body);
        heliusWalletService.processWebhook(body);
        return ResponseEntity.ok().build();
    }
}
