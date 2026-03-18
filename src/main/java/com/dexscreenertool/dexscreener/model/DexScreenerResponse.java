package com.dexscreenertool.dexscreener.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * Outgoing response body.
 * Contains the top 3 pairs ranked by 5m spike and the top 3 ranked by 1h spike.
 */
@Data
@NoArgsConstructor
public class DexScreenerResponse {
    private List<PairData> top3;
}
