package com.dexscreenertool.dexscreener.service;

import com.dexscreenertool.dexscreener.client.DexScreenerClient;
import com.dexscreenertool.dexscreener.model.DexScreenerRequest;
import com.dexscreenertool.dexscreener.model.DexScreenerResponse;
import com.dexscreenertool.dexscreener.model.PairData;
import com.dexscreenertool.dexscreener.model.SpikeAlert;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DexScreenerService {

    private final DexScreenerClient dexScreenerClient;
    private final TelegramNotificationService telegramNotificationService;

    // Injected from application.properties
    @org.springframework.beans.factory.annotation.Value("${dexscreener.chainId}")
    private String chainId;

    @org.springframework.beans.factory.annotation.Value("${dexscreener.pairAddresses}")
    private List<String> pairAddresses;

    // Stores the previous volume snapshot keyed by pairAddress
    // Used to detect new spikes — only alert when volume5m is higher than last run
    private final Map<String, PairData> previousSnapshot = new HashMap<>();

    /**
     * Main entry point — called by the scheduler every 30 minutes (or the controller manually).
     * Reads pair addresses and chainId from application.properties.
     */
    public List<PairData> monitorPairs() {
        // 1. One batch call — fetches all configured pair addresses at once
        List<PairData> livePairs = dexScreenerClient.fetchPairs(chainId, pairAddresses);
        List<PairData> truePairs = livePairs.stream().filter(s -> s.getMarketCap() > 50000).toList();
        List<PairData> response = new ArrayList<>();

        for (PairData pair : truePairs) {
            PairData previous = previousSnapshot.get(pair.getPairAddress());

            boolean volumeAboveThreshold = pair.getVolume5m() > 2500;
            //boolean isNewSpike = previous == null || pair.getVolume5m() > previous.getVolume5m();

            if (volumeAboveThreshold) {
                telegramNotificationService.send(buildMessage(pair));
                response.add(pair);
            }

            // update snapshot so next run can compare against current values
            previousSnapshot.put(pair.getPairAddress(), pair);
        }

        return response;
    }

    private String buildMessage(PairData current) {
        String message = new StringBuilder()
                .append("Token: ").append(current.getBaseTokenSymbol()).append("\n")
                .append("Pair: ").append(current.getPairAddress()).append("\n")
                .append("Market Cap: $").append(current.getMarketCap())
                .toString();
                
        return message;
    }
}
