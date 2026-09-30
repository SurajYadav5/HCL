package com.portfoliopro.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioSummaryResponse {
    private BigDecimal totalInvested;
    private BigDecimal totalCurrentValue;
    private BigDecimal totalUnrealizedPnl;
    private BigDecimal totalUnrealizedPnlPct;
    private BigDecimal totalRealizedPnl;
    private BigDecimal cashBalance;
    private BigDecimal totalPortfolioValue; // Cash + Invested
    private Long totalPositions;
    private Long totalTrades;
    private List<PortfolioItemResponse> holdings;
}
