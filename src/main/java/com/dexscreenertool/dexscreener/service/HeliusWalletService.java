package com.dexscreenertool.dexscreener.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Manages a Helius enhanced webhook that watches configured Solana wallet addresses.
 *
 * On startup, the service checks whether a webhook already exists for this app's URL.
 * If one is found it updates the address list; otherwise it creates a new webhook.
 *
 * When Helius POSTs a transaction event the controller calls {@link #processWebhook(String)}.
 * A "buy" is identified as a SWAP where a tracked wallet is the fee-payer and receives
 * tokens (incoming tokenTransfer where toUserAccount matches the wallet).
 *
 * Required properties:
 *   helius.api.key        — your Helius API key
 *   helius.webhook.url    — the public URL Helius will POST to (e.g. https://your-server/helius/webhook)
 *   helius.tracked.wallets — comma-separated Solana wallet addresses to monitor
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HeliusWalletService {

    private static final String HELIUS_WEBHOOKS_URL = "https://api-mainnet.helius-rpc.com/v0/webhooks";
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final HttpClient httpClient = HttpClient.newHttpClient();

    private final TelegramNotificationService telegramNotificationService;

    @Value("${helius.api.key}")
    private String apiKey;

    @Value("${helius.webhook.url}")
    private String webhookUrl;

    @Value("${helius.tracked.wallets}")
    private List<String> trackedWallets;

    // Optional — set to a secret string; Helius will include it as the Authorization header
    // on every POST so you can reject requests not from Helius.
    @Value("${helius.auth.header:}")
    private String authHeader;

    // ---------------------------------------------------------------------------
    // Startup — register or update the Helius webhook
    // ---------------------------------------------------------------------------

    @PostConstruct
    public void registerWebhook() {
        if (trackedWallets == null || trackedWallets.isEmpty()) {
            log.warn("No tracked wallets configured — skipping Helius webhook registration.");
            return;
        }
        try {
            String existingId = findExistingWebhookId();
            if (existingId != null) {
                updateWebhook(existingId);
                log.info("Helius webhook updated (id={})", existingId);
            } else {
                String newId = createWebhook();
                log.info("Helius webhook created (id={}). Save this if you want to reuse it: {}", newId, newId);
            }
        } catch (Exception e) {
            log.error("Failed to register Helius webhook: {}", e.getMessage(), e);
        }
    }

    /**
     * Returns the webhook ID whose webhookURL matches this app's configured URL, or null.
     */
    private String findExistingWebhookId() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(HELIUS_WEBHOOKS_URL + "?api-key=" + apiKey))
                .GET()
                .header("Accept", "application/json")
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) return null;

        JsonNode root = objectMapper.readTree(response.body());
        if (!root.isArray()) return null;

        for (JsonNode hook : root) {
            if (webhookUrl.equals(hook.path("webhookURL").asText())) {
                return hook.path("webhookID").asText(null);
            }
        }
        return null;
    }

    private String createWebhook() throws Exception {
        String body = buildWebhookBody();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(HELIUS_WEBHOOKS_URL + "?api-key=" + apiKey))
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200 && response.statusCode() != 201) {
            throw new RuntimeException("Helius create webhook failed: " + response.statusCode() + " " + response.body());
        }
        JsonNode json = objectMapper.readTree(response.body());
        return json.path("webhookID").asText();
    }

    private void updateWebhook(String webhookId) throws Exception {
        String body = buildWebhookBody();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(HELIUS_WEBHOOKS_URL + "/" + webhookId + "?api-key=" + apiKey))
                .PUT(HttpRequest.BodyPublishers.ofString(body))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new RuntimeException("Helius update webhook failed: " + response.statusCode() + " " + response.body());
        }
    }

    private String buildWebhookBody() throws Exception {
        ObjectNode body = objectMapper.createObjectNode();
        body.put("webhookURL", webhookUrl);
        body.put("webhookType", "enhanced");

        ArrayNode txTypes = objectMapper.createArrayNode();
        txTypes.add("SWAP");
        body.set("transactionTypes", txTypes);

        ArrayNode addresses = objectMapper.createArrayNode();
        trackedWallets.forEach(addresses::add);
        body.set("accountAddresses", addresses);

        if (authHeader != null && !authHeader.isBlank()) {
            body.put("authHeader", authHeader);
        }

        return objectMapper.writeValueAsString(body);
    }

    // ---------------------------------------------------------------------------
    // Webhook payload processing — called by the controller on each POST
    // ---------------------------------------------------------------------------

    /**
     * Parses the Helius enhanced transaction array and sends a Telegram alert for any
     * SWAP transaction where a tracked wallet receives tokens (i.e. a buy).
     *
     * Expected payload shape (array of enhanced transactions):
     * [
     *   {
     *     "type": "SWAP",
     *     "source": "RAYDIUM",
     *     "feePayer": "walletAddress",
     *     "signature": "txSignature",
     *     "timestamp": 1234567890,
     *     "tokenTransfers": [
     *       { "fromUserAccount": "...", "toUserAccount": "walletAddress",
     *         "tokenAmount": 1000000.0, "mint": "tokenMint" }
     *     ],
     *     "nativeTransfers": [
     *       { "fromUserAccount": "walletAddress", "toUserAccount": "...", "amount": 1000000000 }
     *     ]
     *   }
     * ]
     */
    /**
     * Validates that the request came from Helius by checking the Authorization header.
     * Returns false (and logs a warning) if the header is configured but doesn't match.
     */
    public boolean isValidRequest(String incomingAuthHeader) {
        if (authHeader == null || authHeader.isBlank()) return true; // no secret configured, skip check
        return authHeader.equals(incomingAuthHeader);
    }

    public void processWebhook(String body) {
        try {
            JsonNode root = objectMapper.readTree(body);
            if (!root.isArray()) {
                log.warn("Unexpected Helius webhook payload shape (not array)");
                return;
            }

            Set<String> walletSet = trackedWallets.stream()
                    .map(String::toLowerCase)
                    .collect(Collectors.toSet());

            for (JsonNode tx : root) {
                String type = tx.path("type").asText();
                if (!"SWAP".equalsIgnoreCase(type)) continue;

                String feePayer = tx.path("feePayer").asText();
                if (!walletSet.contains(feePayer.toLowerCase())) continue;

                // Find tokens received by the tracked wallet
                JsonNode tokenTransfers = tx.path("tokenTransfers");
                for (JsonNode transfer : tokenTransfers) {
                    String toAccount = transfer.path("toUserAccount").asText();
                    if (!feePayer.equalsIgnoreCase(toAccount)) continue;

                    String mint = transfer.path("mint").asText();
                    double tokenAmount = transfer.path("tokenAmount").asDouble();
                    String source = tx.path("source").asText();
                    String signature = tx.path("signature").asText();

                    // Calculate SOL spent from nativeTransfers (wallet → pool)
                    double solSpent = StreamSupport.stream(tx.path("nativeTransfers").spliterator(), false)
                            .filter(nt -> feePayer.equalsIgnoreCase(nt.path("fromUserAccount").asText()))
                            .mapToLong(nt -> nt.path("amount").asLong())
                            .sum() / 1_000_000_000.0; // lamports → SOL

                    telegramNotificationService.send(buildBuyMessage(feePayer, mint, tokenAmount, solSpent, source, signature));
                }
            }
        } catch (Exception e) {
            log.error("Error processing Helius webhook payload: {}", e.getMessage(), e);
        }
    }

    private String buildBuyMessage(String wallet, String mint, double tokenAmount,
                                   double solSpent, String dex, String signature) {
        String shortWallet = wallet.length() > 8
                ? wallet.substring(0, 4) + "..." + wallet.substring(wallet.length() - 4)
                : wallet;
        String shortSig = signature.length() > 12
                ? signature.substring(0, 6) + "..." + signature.substring(signature.length() - 6)
                : signature;

        return new StringBuilder()
                .append("\uD83D\uDFE2 Wallet Buy Detected\n")
                .append("Wallet: ").append(shortWallet).append("\n")
                .append("Token:  ").append(mint).append("\n")
                .append("Amount: ").append(String.format("%,.2f", tokenAmount)).append(" tokens\n")
                .append("Spent:  ").append(String.format("%.4f", solSpent)).append(" SOL\n")
                .append("DEX:    ").append(dex).append("\n")
                .append("Tx:     ").append(shortSig)
                .toString();
    }
}
