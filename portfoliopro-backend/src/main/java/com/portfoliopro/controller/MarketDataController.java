package com.portfoliopro.controller;

import com.portfoliopro.dto.response.ApiResponse;
import com.portfoliopro.dto.response.StockQuoteResponse;
import com.portfoliopro.service.MarketDataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/market")
@RequiredArgsConstructor
@Tag(name = "Market Data", description = "Stock quotes, search and historical data APIs")
@SecurityRequirement(name = "bearerAuth")
public class MarketDataController {

    private final MarketDataService marketDataService;

    @GetMapping("/quote/{symbol}")
    @Operation(summary = "Get real-time stock quote")
    public ResponseEntity<ApiResponse<StockQuoteResponse>> getQuote(@PathVariable String symbol) {
        StockQuoteResponse quote = marketDataService.getQuote(symbol.toUpperCase());
        return ResponseEntity.ok(ApiResponse.success(quote));
    }

    @GetMapping("/search")
    @Operation(summary = "Search stocks by keyword")
    public ResponseEntity<ApiResponse<List<Map<String, String>>>> searchStocks(
            @RequestParam String q) {
        List<Map<String, String>> results = marketDataService.searchSymbols(q);
        return ResponseEntity.ok(ApiResponse.success(results));
    }

    @GetMapping("/history/{symbol}")
    @Operation(summary = "Get OHLCV historical data for charting")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getHistory(
            @PathVariable String symbol,
            @RequestParam(defaultValue = "compact") String outputSize) {
        List<Map<String, Object>> history = marketDataService.getDailyTimeSeries(symbol.toUpperCase(), outputSize);
        return ResponseEntity.ok(ApiResponse.success(history));
    }

    @GetMapping("/quotes/batch")
    @Operation(summary = "Get quotes for multiple symbols")
    public ResponseEntity<ApiResponse<List<StockQuoteResponse>>> getBatchQuotes(
            @RequestParam List<String> symbols) {
        List<StockQuoteResponse> quotes = symbols.stream()
                .map(s -> marketDataService.getQuote(s.toUpperCase()))
                .toList();
        return ResponseEntity.ok(ApiResponse.success(quotes));
    }

    @GetMapping("/market-overview")
    @Operation(summary = "Get overview of major indices and popular stocks")
    public ResponseEntity<ApiResponse<List<StockQuoteResponse>>> getMarketOverview() {
        List<String> majorSymbols = List.of("AAPL", "MSFT", "GOOGL", "AMZN", "TSLA", "NVDA", "META", "JPM");
        List<StockQuoteResponse> quotes = majorSymbols.stream()
                .map(marketDataService::getQuote)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(quotes));
    }
}
