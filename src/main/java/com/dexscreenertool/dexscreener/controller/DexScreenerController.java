package com.dexscreenertool.dexscreener.controller;

import com.dexscreenertool.dexscreener.model.DexScreenerResponse;
import com.dexscreenertool.dexscreener.service.DexScreenerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/dexscreener")
@RequiredArgsConstructor
public class DexScreenerController {

    private final DexScreenerService dexScreenerService;

    /**
     * GET /dexscreener/monitor
     * Manually triggers a volume spike scan using the pair addresses configured in application.properties.
     * The scheduler also calls the same service method automatically every 30 minutes.
     */
    @GetMapping("/monitor")
    public DexScreenerResponse monitorPairs() {
        return dexScreenerService.monitorPairs();
    }
}
