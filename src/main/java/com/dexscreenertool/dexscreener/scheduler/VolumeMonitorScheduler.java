package com.dexscreenertool.dexscreener.scheduler;

import com.dexscreenertool.dexscreener.service.DexScreenerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class VolumeMonitorScheduler {

    private final DexScreenerService dexScreenerService;

    /**
     * Runs every 5 minutes so the 5m volume window is checked on every fresh interval.
     * The 1h check also runs here but is rate-limited inside the service (max once per 55 min per pair).
     *
     * fixedDelay = 5 minutes in milliseconds (5 * 60 * 1000)
     * initialDelay = 0 — runs once immediately on startup
     */
    @Scheduled(fixedDelay = 300_000, initialDelay = 0)
    public void run() {
        log.info("Running volume spike scan...");
        dexScreenerService.monitorPairs();
        log.info("Scan complete.");
    }
}
