package com.dexscreenertool.dexscreener.client;

import com.dexscreenertool.dexscreener.model.PairData;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * Talks to the DexScreener public API.
 *
 * Batch endpoint:
 *   GET /latest/dex/pairs/{chainId}/{addr1},{addr2},...
 *   Returns: { "pairs": [ { pairAddress, chainId, baseToken, quoteToken, volume: { m5, h1, h6, h24 } }, ... ] }
 *
 * Up to 30 addresses per request. No API key needed.
 */
@Component
public class DexScreenerClient {

    private static final String BASE_URL = "https://api.dexscreener.com/latest/dex/pairs";
    private static final HttpClient httpClient = HttpClient.newHttpClient();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Fetches all provided pair addresses in a single batch HTTP call.
     * Parses the "pairs" array from the response and returns a list of PairData.
     */
    public List<PairData> fetchPairs(String chainId, List<String> pairAddresses) {
        // Join all addresses with commas — DexScreener accepts up to 30 in one call
        String joined = String.join(",", pairAddresses);
        String url = BASE_URL + "/" + chainId + "/" + joined;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return parsePairs(response.body());
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    /**
     * Parses the raw JSON body into a list of PairData.
     *
     * JSON shape:
     * {
     *   "pairs": [
     *     {
     *       "pairAddress": "...",
     *       "chainId": "solana",
     *       "dexId": "raydium",
     *       "baseToken":  { "symbol": "..." },
     *       "quoteToken": { "symbol": "..." },
     *       "volume": { "m5": 0.0, "h1": 0.0, "h6": 0.0, "h24": 0.0 }
     *     }
     *   ]
     * }
     */
    private List<PairData> parsePairs(String body) throws Exception {
        List<PairData> result = new ArrayList<>();
        JsonNode root = objectMapper.readTree(body);
        JsonNode pairs = root.path("pairs");

        for (JsonNode node : pairs) {
            PairData pair = new PairData();
            pair.setPairAddress(node.path("pairAddress").asText());
            pair.setChainId(node.path("chainId").asText());
            pair.setDexId(node.path("dexId").asText());
            pair.setBaseTokenSymbol(node.path("baseToken").path("symbol").asText());
            pair.setQuoteTokenSymbol(node.path("quoteToken").path("symbol").asText());

            JsonNode volume = node.path("volume");
            pair.setVolume5m(volume.path("m5").asDouble());
            pair.setVolume1h(volume.path("h1").asDouble());
            pair.setVolume6h(volume.path("h6").asDouble());
            pair.setVolume24h(volume.path("h24").asDouble());

            result.add(pair);
        }
        return result;
    }
}
