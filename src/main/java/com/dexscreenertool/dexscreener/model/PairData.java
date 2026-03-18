package com.dexscreenertool.dexscreener.model;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a single trading pair returned by the DexScreener API.
 * Maps to the fields inside each element of the "pairs" array in the response.
 */
@Data
@NoArgsConstructor
public class PairData {
    private String pairAddress;
    private String chainId;
    private String dexId;
    private String baseTokenSymbol;
    private String quoteTokenSymbol;

    // Volume fields from volume.h5m / volume.h1 / volume.h6 / volume.h24 in the JSON
    private double volume5m;
    private double volume1h;
    private double volume6h;
    private double volume24h;

    // TODO: add priceUsd, liquidity, txns if you want richer alerts
}
