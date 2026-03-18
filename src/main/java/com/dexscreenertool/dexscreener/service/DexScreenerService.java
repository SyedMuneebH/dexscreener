package com.dexscreenertool.dexscreener.service;

import com.dexscreenertool.dexscreener.client.DexScreenerClient;
import com.dexscreenertool.dexscreener.model.DexScreenerRequest;
import com.dexscreenertool.dexscreener.model.DexScreenerResponse;
import com.dexscreenertool.dexscreener.model.PairData;
import com.dexscreenertool.dexscreener.model.SpikeAlert;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
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

    @org.springframework.beans.factory.annotation.Value("${dexscreener.topN:3}")
    private int topN;

    // Stores the previous volume snapshot keyed by pairAddress
    // TODO: declare as Map<String, PairData> previousSnapshot = new HashMap<>()

    /**
     * Main entry point — called by the scheduler every 30 minutes (or the controller manually).
     * Reads pair addresses and chainId from application.properties.
     *
     * Hints:
     * 1. Call dexScreenerClient.fetchPairs(chainId, pairAddresses) to get live data
     * 2. For each PairData, call detectSpike(live, previousSnapshot.get(live.getPairAddress()))
     * 3. Collect all non-null SpikeAlerts into a list
     * 4. Sort by spikePercent5m descending → take top topN → store as top3By5mSpike
     * 5. Sort by spikePercent1h descending → take top topN → store as top3By1hSpike
     * 6. Update previousSnapshot with the fresh PairData for next call
     * 7. For each alert in the combined top lists, call telegramNotificationService.send(...)
     * 8. Build and return the DexScreenerResponse
     */
    public DexScreenerResponse monitorPairs() {
        // 1. One batch call — fetches all configured pair addresses at once
        List<PairData> livePairs = dexScreenerClient.fetchPairs(chainId, pairAddresses);

        // TODO: step 2 — for each PairData in livePairs, call detectSpike(live, previousSnapshot.get(live.getPairAddress()))
        // TODO: step 3 — collect non-null SpikeAlerts into a list
        // TODO: step 4 — sort by spikePercent5m desc, take top topN
        // TODO: step 5 — sort by spikePercent1h desc, take top topN
        // TODO: step 6 — update previousSnapshot for each live pair
        // TODO: step 7 — send Telegram notification for each top alert
        // TODO: step 8 — populate and return DexScreenerResponse

        return new DexScreenerResponse();
    }

    /**
     * Compares current and previous pair data to build a SpikeAlert.
     *
     * Hints:
     * 1. If previous is null, no spike can be calculated — return null
     * 2. spikePercent5m = ((current.volume5m - previous.volume5m) / previous.volume5m) * 100
     * 3. Same formula for spikePercent1h
     * 4. Populate and return a SpikeAlert — return null if both spikes are <= 0
     */
    private SpikeAlert detectSpike(PairData current, PairData previous) {
        // TODO: implement
        return null;
    }
}
