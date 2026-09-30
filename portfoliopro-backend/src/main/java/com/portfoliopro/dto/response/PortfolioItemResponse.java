package com.portfoliopro.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioItemResponse {
    private Long id;
    private String symbol;
    private String companyName;
    private String sector;
    private Integer quantity;
    private BigDecimal averageCost;
    private BigDecimal totalInvested;
    private BigDecimal currentPrice;
    private BigDecimal currentValue;
    private BigDecimal unrealizedPnl;
    private BigDecimal unrealizedPnlPct;
    private BigDecimal realizedPnl;
    private BigDecimal allocationPct; // Percentage of total portfolio
    private LocalDateTime createdAt;
}
