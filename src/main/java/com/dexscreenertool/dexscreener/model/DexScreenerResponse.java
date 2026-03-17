package com.dexscreenertool.dexscreener.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DexScreenerResponse {
    // Define fields for outgoing JSON response
    private double volume;
    private boolean spikeDetected;
    // Add other fields as needed
}
