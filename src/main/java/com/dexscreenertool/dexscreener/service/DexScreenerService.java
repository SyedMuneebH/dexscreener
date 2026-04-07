package com.dexscreenertool.dexscreener.service;

import com.dexscreenertool.dexscreener.client.DexScreenerClient;
import com.dexscreenertool.dexscreener.model.PairData;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DexScreenerService {

    private final DexScreenerClient dexScreenerClient;
    private final TelegramNotificationService telegramNotificationService;

    @Value("${dexscreener.chainId}")
    private String chainId;

    @Value("${dexscreener.pairAddresses}")
    private List<String> pairAddresses;

    // Volume thresholds
    private static final double VOLUME_5M_THRESHOLD = 5_000;
    private static final double VOLUME_1H_THRESHOLD = 50_000;

    // Previous volume snapshot per pairAddress — used to confirm volume is actively rising
    private final Map<String, PairData> previousSnapshot = new HashMap<>();

    // Tracks when a 1h spike alert was last sent per pair — prevents per-5min spam
    private final Map<String, Instant> lastHourAlertTime = new HashMap<>();

    /**
     * Called by the scheduler every 5 minutes (and by the controller on demand).
     *
     * Spike rules:
     *   5m  — volume5m  > 5,000  AND volume is higher than the previous snapshot
     *   1h  — volume1h  > 50,000 AND no alert was sent for this pair in the last 55 minutes
     *
     * Both checks run in a single API call since DexScreener returns all volume windows at once.
     */
    public List<PairData> monitorPairs() {
        List<PairData> livePairs = dexScreenerClient.fetchPairs(chainId, pairAddresses);
        // Only track pairs that have a meaningful market cap
        List<PairData> activePairs = livePairs.stream()
                .filter(p -> p.getMarketCap() > 50_000)
                .toList();
        List<PairData> alerted = new ArrayList<>();
        Instant now = Instant.now();

        for (PairData pair : activePairs) {
            PairData prev = previousSnapshot.get(pair.getPairAddress());
            boolean pairAlerted = false;

            // --- 5-minute spike ---
            boolean spike5m = pair.getVolume5m() > VOLUME_5M_THRESHOLD
                    && (prev == null || pair.getVolume5m() > prev.getVolume5m());

            if (spike5m) {
                telegramNotificationService.send(buildMessage(pair, "5m", pair.getVolume5m()));
                pairAlerted = true;
            }

            // --- 1-hour spike ---
            Instant lastAlert1h = lastHourAlertTime.get(pair.getPairAddress());
            boolean cooldownExpired = lastAlert1h == null
                    || Duration.between(lastAlert1h, now).toMinutes() >= 55;
            boolean spike1h = pair.getVolume1h() > VOLUME_1H_THRESHOLD && cooldownExpired;

            if (spike1h) {
                telegramNotificationService.send(buildMessage(pair, "1h", pair.getVolume1h()));
                lastHourAlertTime.put(pair.getPairAddress(), now);
                pairAlerted = true;
            }

            if (pairAlerted) alerted.add(pair);

            // Always update snapshot for next run
            previousSnapshot.put(pair.getPairAddress(), pair);
        }

        return alerted;
    }

    private String buildMessage(PairData pair, String window, double volume) {
        return new StringBuilder()
                .append("\uD83D\uDEA8 Volume Spike Alert (").append(window).append(")\n")
                .append("Token:      ").append(pair.getBaseTokenSymbol()).append("/").append(pair.getQuoteTokenSymbol()).append("\n")
                .append("Pair:       ").append(pair.getPairAddress()).append("\n")
                .append("Market Cap: $").append(String.format("%,.0f", pair.getMarketCap())).append("\n")
                .append("Volume (").append(window).append("): $").append(String.format("%,.0f", volume))
                .toString();
    }
}
