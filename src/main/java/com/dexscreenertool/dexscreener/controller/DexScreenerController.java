package com.dexscreenertool.dexscreener.controller;

import com.dexscreenertool.dexscreener.model.PairData;
import com.dexscreenertool.dexscreener.service.DexScreenerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/dexscreener")
@RequiredArgsConstructor
public class DexScreenerController {

    private final DexScreenerService dexScreenerService;

    /**
     * GET /dexscreener/monitor
     * Manually triggers a volume spike scan.
     * Returns 200 with matching pairs, or 200 with a message if nothing qualifies.
     */
    @GetMapping("/monitor")
    public ResponseEntity<?> monitorPairs() {
        List<PairData> pairs = dexScreenerService.monitorPairs();

        if (!pairs.isEmpty()) {
            return ResponseEntity.ok(pairs);
        }
        return ResponseEntity.ok(Map.of("message", "nothing over 5k volume in a 5minute timeframe."));
    }
}
