package com.dexscreenertool.dexscreener.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * Incoming request body.
 * The caller supplies a watchlist of pair addresses to evaluate.
 * If the list is empty, the service will fall back to a default set.
 */
@Data
@NoArgsConstructor
public class DexScreenerRequest {

    // List of pair addresses to monitor, e.g. ["0xabc...", "0xdef..."]
    private List<String> pairAddresses;

    // How many top spikers to return (default 3)
    private int topN = 3;
}
