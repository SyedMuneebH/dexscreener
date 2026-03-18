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

    // Top 3 pairs with the largest 5-minute volume spike
    private List<SpikeAlert> top3By5mSpike;

    // Top 3 pairs with the largest 1-hour volume spike
    private List<SpikeAlert> top3By1hSpike;

    // TODO: add a timestamp or scanId so responses are traceable
}
