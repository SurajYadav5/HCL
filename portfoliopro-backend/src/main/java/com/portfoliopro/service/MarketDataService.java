package com.portfoliopro.service;

import com.portfoliopro.dto.response.StockQuoteResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class MarketDataService {

    @Value("${app.alphavantage.api-key}")
    private String apiKey;

    @Value("${app.alphavantage.base-url}")
    private String baseUrl;

    private final WebClient.Builder webClientBuilder;

    /**
     * Fetch real-time global quote from Alpha Vantage.
     * Cached for 60 seconds to respect rate limits.
     */
    @Cacheable(value = "stockQuotes", key = "#symbol")
    public StockQuoteResponse getQuote(String symbol) {
        try {
            log.info("Fetching quote for symbol: {}", symbol);
            Map<String, Object> response = webClientBuilder.build()
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .scheme("https")
                            .host("www.alphavantage.co")
                            .path("/query")
                            .queryParam("function", "GLOBAL_QUOTE")
                            .queryParam("symbol", symbol)
                            .queryParam("apikey", apiKey)
                            .build())
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            return parseQuoteResponse(symbol, response);
        } catch (Exception e) {
            log.error("Error fetching quote for {}: {}", symbol, e.getMessage());
            return getMockQuote(symbol);
        }
    }

    /**
     * Search for stock symbols by keyword.
     */
    public List<Map<String, String>> searchSymbols(String keywords) {
        try {
            Map<String, Object> response = webClientBuilder.build()
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .scheme("https")
                            .host("www.alphavantage.co")
                            .path("/query")
                            .queryParam("function", "SYMBOL_SEARCH")
                            .queryParam("keywords", keywords)
                            .queryParam("apikey", apiKey)
                            .build())
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            return parseSearchResults(response);
        } catch (Exception e) {
            log.error("Error searching symbols: {}", e.getMessage());
            return getDefaultStocks();
        }
    }

    /**
     * Get daily time series (OHLCV) for charting.
     */
    @Cacheable(value = "timeSeries", key = "#symbol + '_' + #outputSize")
    public List<Map<String, Object>> getDailyTimeSeries(String symbol, String outputSize) {
        try {
            Map<String, Object> response = webClientBuilder.build()
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .scheme("https")
                            .host("www.alphavantage.co")
                            .path("/query")
                            .queryParam("function", "TIME_SERIES_DAILY")
                            .queryParam("symbol", symbol)
                            .queryParam("outputsize", outputSize != null ? outputSize : "compact")
                            .queryParam("apikey", apiKey)
                            .build())
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            return parseTimeSeries(response);
        } catch (Exception e) {
            log.error("Error fetching time series for {}: {}", symbol, e.getMessage());
            return generateMockTimeSeries(symbol, 90);
        }
    }

    // ─── Parsers ─────────────────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private StockQuoteResponse parseQuoteResponse(String symbol, Map<String, Object> response) {
        if (response == null || !response.containsKey("Global Quote")) {
            log.warn("Empty response from Alpha Vantage for {}, using mock", symbol);
            return getMockQuote(symbol);
        }

        Map<String, String> quote = (Map<String, String>) response.get("Global Quote");
        if (quote == null || quote.isEmpty()) {
            return getMockQuote(symbol);
        }

        return StockQuoteResponse.builder()
                .symbol(quote.getOrDefault("01. symbol", symbol))
                .price(parseBigDecimal(quote.get("05. price")))
                .open(parseBigDecimal(quote.get("02. open")))
                .high(parseBigDecimal(quote.get("03. high")))
                .low(parseBigDecimal(quote.get("04. low")))
                .previousClose(parseBigDecimal(quote.get("08. previous close")))
                .change(parseBigDecimal(quote.get("09. change")))
                .changePercent(parseChangePercent(quote.get("10. change percent")))
                .volume(parseLong(quote.get("06. volume")))
                .lastUpdated(quote.get("07. latest trading day"))
                .companyName(symbol) // Alpha Vantage basic quote doesn't include name
                .build();
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, String>> parseSearchResults(Map<String, Object> response) {
        if (response == null || !response.containsKey("bestMatches")) {
            return getDefaultStocks();
        }
        List<Map<String, String>> matches = (List<Map<String, String>>) response.get("bestMatches");
        List<Map<String, String>> results = new ArrayList<>();

        for (Map<String, String> match : matches) {
            Map<String, String> result = new HashMap<>();
            result.put("symbol", match.getOrDefault("1. symbol", ""));
            result.put("name", match.getOrDefault("2. name", ""));
            result.put("type", match.getOrDefault("3. type", ""));
            result.put("region", match.getOrDefault("4. region", ""));
            result.put("currency", match.getOrDefault("8. currency", "USD"));
            results.add(result);
        }
        return results;
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> parseTimeSeries(Map<String, Object> response) {
        if (response == null || !response.containsKey("Time Series (Daily)")) {
            return generateMockTimeSeries("UNKNOWN", 90);
        }

        Map<String, Map<String, String>> timeSeries =
                (Map<String, Map<String, String>>) response.get("Time Series (Daily)");

        List<Map<String, Object>> result = new ArrayList<>();
        timeSeries.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> {
                    Map<String, Object> point = new HashMap<>();
                    point.put("date", entry.getKey());
                    point.put("open", parseBigDecimal(entry.getValue().get("1. open")));
                    point.put("high", parseBigDecimal(entry.getValue().get("2. high")));
                    point.put("low", parseBigDecimal(entry.getValue().get("3. low")));
                    point.put("close", parseBigDecimal(entry.getValue().get("4. close")));
                    point.put("volume", parseLong(entry.getValue().get("5. volume")));
                    result.add(point);
                });
        return result;
    }

    // ─── Mock / Fallback Data ─────────────────────────────────────────────────────

    public StockQuoteResponse getMockQuote(String symbol) {
        Random rand = new Random(symbol.hashCode());
        double base = 50 + rand.nextDouble() * 450;
        double change = (rand.nextDouble() - 0.48) * 10;
        BigDecimal price = BigDecimal.valueOf(base + change).setScale(2, java.math.RoundingMode.HALF_UP);
        BigDecimal prev = BigDecimal.valueOf(base).setScale(2, java.math.RoundingMode.HALF_UP);
        BigDecimal ch = price.subtract(prev);
        BigDecimal chPct = ch.divide(prev, 4, java.math.RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));

        return StockQuoteResponse.builder()
                .symbol(symbol)
                .companyName(getCompanyName(symbol))
                .price(price)
                .open(prev.add(BigDecimal.valueOf(rand.nextDouble() * 5)))
                .high(price.add(BigDecimal.valueOf(rand.nextDouble() * 8)))
                .low(price.subtract(BigDecimal.valueOf(rand.nextDouble() * 8)))
                .previousClose(prev)
                .change(ch)
                .changePercent(chPct)
                .volume((long)(rand.nextInt(50000000) + 1000000))
                .marketCap(price.multiply(BigDecimal.valueOf(rand.nextInt(10000000000L > 0 ? 10000000000 : 1))))
                .peRatio(BigDecimal.valueOf(15 + rand.nextDouble() * 30))
                .week52High(price.multiply(BigDecimal.valueOf(1.3)))
                .week52Low(price.multiply(BigDecimal.valueOf(0.7)))
                .lastUpdated(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .exchange("NASDAQ")
                .build();
    }

    public List<Map<String, Object>> generateMockTimeSeries(String symbol, int days) {
        List<Map<String, Object>> data = new ArrayList<>();
        Random rand = new Random(symbol.hashCode());
        double price = 100 + rand.nextDouble() * 300;

        for (int i = days; i >= 0; i--) {
            LocalDateTime date = LocalDateTime.now().minusDays(i);
            double open = price;
            double move = (rand.nextDouble() - 0.48) * 5;
            double close = Math.max(1, open + move);
            double high = Math.max(open, close) + rand.nextDouble() * 3;
            double low = Math.min(open, close) - rand.nextDouble() * 3;

            Map<String, Object> point = new HashMap<>();
            point.put("date", date.toLocalDate().toString());
            point.put("open", BigDecimal.valueOf(open).setScale(2, java.math.RoundingMode.HALF_UP));
            point.put("high", BigDecimal.valueOf(high).setScale(2, java.math.RoundingMode.HALF_UP));
            point.put("low", BigDecimal.valueOf(Math.max(0.01, low)).setScale(2, java.math.RoundingMode.HALF_UP));
            point.put("close", BigDecimal.valueOf(close).setScale(2, java.math.RoundingMode.HALF_UP));
            point.put("volume", (long)(rand.nextInt(10000000) + 500000));

            data.add(point);
            price = close;
        }
        return data;
    }

    private List<Map<String, String>> getDefaultStocks() {
        return List.of(
            Map.of("symbol","AAPL","name","Apple Inc.","type","Equity","region","United States","currency","USD"),
            Map.of("symbol","MSFT","name","Microsoft Corp.","type","Equity","region","United States","currency","USD"),
            Map.of("symbol","GOOGL","name","Alphabet Inc.","type","Equity","region","United States","currency","USD"),
            Map.of("symbol","AMZN","name","Amazon.com Inc.","type","Equity","region","United States","currency","USD"),
            Map.of("symbol","TSLA","name","Tesla Inc.","type","Equity","region","United States","currency","USD"),
            Map.of("symbol","NVDA","name","NVIDIA Corp.","type","Equity","region","United States","currency","USD"),
            Map.of("symbol","META","name","Meta Platforms","type","Equity","region","United States","currency","USD"),
            Map.of("symbol","JPM","name","JPMorgan Chase","type","Equity","region","United States","currency","USD")
        );
    }

    private String getCompanyName(String symbol) {
        return Map.of(
            "AAPL", "Apple Inc.", "MSFT", "Microsoft Corp.", "GOOGL", "Alphabet Inc.",
            "AMZN", "Amazon.com Inc.", "TSLA", "Tesla Inc.", "NVDA", "NVIDIA Corp.",
            "META", "Meta Platforms", "JPM", "JPMorgan Chase", "NFLX", "Netflix Inc.",
            "AMD", "Advanced Micro Devices"
        ).getOrDefault(symbol, symbol + " Corp.");
    }

    // ─── Utility Parsers ──────────────────────────────────────────────────────────

    private BigDecimal parseBigDecimal(String value) {
        try {
            return value != null ? new BigDecimal(value.trim()) : BigDecimal.ZERO;
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    private BigDecimal parseChangePercent(String value) {
        try {
            if (value == null) return BigDecimal.ZERO;
            return new BigDecimal(value.replace("%", "").trim());
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    private Long parseLong(String value) {
        try {
            return value != null ? Long.parseLong(value.trim()) : 0L;
        } catch (Exception e) {
            return 0L;
        }
    }
}
