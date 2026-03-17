package com.dexscreenertool.dexscreener.controller;

import com.dexscreenertool.dexscreener.model.DexScreenerRequest;
import com.dexscreenertool.dexscreener.model.DexScreenerResponse;
import com.dexscreenertool.dexscreener.service.DexScreenerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/dexscreener")
public class DexScreenerController {

    @Autowired
    private DexScreenerService dexscreenerService;

    @PostMapping("/monitor")
    public DexScreenerResponse monitorPair(@RequestBody DexScreenerRequest request) {
        return dexscreenerService.monitorPair(request);
    }
}
