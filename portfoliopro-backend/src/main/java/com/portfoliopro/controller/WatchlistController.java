package com.portfoliopro.controller;

import com.portfoliopro.dto.response.ApiResponse;
import com.portfoliopro.service.WatchlistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/watchlist")
@RequiredArgsConstructor
@Tag(name = "Watchlist", description = "Manage your stock watchlist")
@SecurityRequirement(name = "bearerAuth")
public class WatchlistController {

    private final WatchlistService watchlistService;

    @GetMapping
    @Operation(summary = "Get user watchlist with live prices")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getWatchlist(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success(
                watchlistService.getWatchlist(userDetails.getUsername())));
    }

    @PostMapping("/{symbol}")
    @Operation(summary = "Add a stock to watchlist")
    public ResponseEntity<ApiResponse<Object>> addToWatchlist(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String symbol,
            @RequestParam(required = false) String companyName) {
        watchlistService.addToWatchlist(userDetails.getUsername(), symbol, companyName);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(symbol + " added to watchlist", null));
    }

    @DeleteMapping("/{symbol}")
    @Operation(summary = "Remove a stock from watchlist")
    public ResponseEntity<ApiResponse<Object>> removeFromWatchlist(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String symbol) {
        watchlistService.removeFromWatchlist(userDetails.getUsername(), symbol);
        return ResponseEntity.ok(ApiResponse.success(symbol + " removed from watchlist", null));
    }
}
