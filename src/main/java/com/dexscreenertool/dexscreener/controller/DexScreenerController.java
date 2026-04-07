package com.dexscreenertool.dexscreener.controller;

import com.dexscreenertool.dexscreener.model.DexScreenerRequest;
import com.dexscreenertool.dexscreener.model.PairData;
import com.dexscreenertool.dexscreener.service.DexScreenerService;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/dexscreener")
public class DexScreenerController {

    @Autowired
    private DexScreenerService dexscreenerService;

    @PostMapping("/monitor")
    public List<PairData> monitorPair(@RequestBody DexScreenerRequest request) {
        return dexscreenerService.monitorPairs();
    }
}
