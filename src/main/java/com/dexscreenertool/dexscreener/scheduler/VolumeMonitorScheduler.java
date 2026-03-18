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
     * Runs automatically every 30 minutes.
     * Delegates entirely to the service — no logic here.
     *
     * fixedDelay = 30 minutes in milliseconds (30 * 60 * 1000)
     * initialDelay = runs once immediately on startup, then every 30 min after
     */
    @Scheduled(fixedDelay = 1_800_000, initialDelay = 0)
    public void run() {
        log.info("Running volume spike scan...");
        dexScreenerService.monitorPairs();
        log.info("Scan complete.");
    }
}
