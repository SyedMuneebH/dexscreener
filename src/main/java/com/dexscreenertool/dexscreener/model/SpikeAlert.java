package com.dexscreenertool.dexscreener.model;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a detected volume spike for a single pair.
 * One of these is created per pair that crosses the spike threshold.
 */
@Data
@NoArgsConstructor
public class SpikeAlert {
    private String pairAddress;
    private String chainId;
    private String pairLabel;       // e.g. "WETH/USDC"

    private double currentVolume5m;
    private double currentVolume1h;

    private double previousVolume5m;
    private double previousVolume1h;

    private double spikePercent5m;  // % change in 5m volume vs previous snapshot
    private double spikePercent1h;  // % change in 1h volume vs previous snapshot

    // TODO: add a timestamp so you know when the alert was generated
}
