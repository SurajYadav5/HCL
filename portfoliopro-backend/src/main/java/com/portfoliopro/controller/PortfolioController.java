package com.portfoliopro.controller;

import com.portfoliopro.dto.response.ApiResponse;
import com.portfoliopro.dto.response.PortfolioSummaryResponse;
import com.portfoliopro.service.PortfolioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/portfolio")
@RequiredArgsConstructor
@Tag(name = "Portfolio", description = "Portfolio management and P&L tracking APIs")
@SecurityRequirement(name = "bearerAuth")
public class PortfolioController {

    private final PortfolioService portfolioService;

    @GetMapping
    @Operation(summary = "Get full portfolio summary with live prices and P&L")
    public ResponseEntity<ApiResponse<PortfolioSummaryResponse>> getPortfolio(
            @AuthenticationPrincipal UserDetails userDetails) {
        PortfolioSummaryResponse summary = portfolioService.getPortfolioSummary(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(summary));
    }
}
