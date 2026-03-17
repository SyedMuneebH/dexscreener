package com.dexscreenertool.dexscreener.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DexScreenerRequest {
    // Define fields for incoming JSON request
    private String pairAddress;
    private String chain;
    // Add other fields as needed
}
