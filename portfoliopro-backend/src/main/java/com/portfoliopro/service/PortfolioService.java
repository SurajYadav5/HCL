package com.portfoliopro.service;

import com.portfoliopro.dto.response.PortfolioItemResponse;
import com.portfoliopro.dto.response.PortfolioSummaryResponse;
import com.portfoliopro.dto.response.StockQuoteResponse;
import com.portfoliopro.entity.Portfolio;
import com.portfoliopro.entity.User;
import com.portfoliopro.exception.ResourceNotFoundException;
import com.portfoliopro.repository.OrderRepository;
import com.portfoliopro.repository.PortfolioRepository;
import com.portfoliopro.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PortfolioService {

    private final PortfolioRepository portfolioRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final MarketDataService marketDataService;

    @Transactional(readOnly = true)
    public PortfolioSummaryResponse getPortfolioSummary(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        List<Portfolio> holdings = portfolioRepository.findByUserIdOrderByCurrentValueDesc(user.getId());

        // Refresh metrics with current market prices
        BigDecimal totalCurrentValue = BigDecimal.ZERO;
        BigDecimal totalInvested = BigDecimal.ZERO;
        BigDecimal totalRealizedPnl = BigDecimal.ZERO;

        for (Portfolio p : holdings) {
            try {
                StockQuoteResponse quote = marketDataService.getQuote(p.getSymbol());
                p.refreshMetrics(quote.getPrice());
                portfolioRepository.save(p);
            } catch (Exception e) {
                log.warn("Could not refresh price for {}: {}", p.getSymbol(), e.getMessage());
            }
            totalCurrentValue = totalCurrentValue.add(
                    p.getCurrentValue() != null ? p.getCurrentValue() : BigDecimal.ZERO);
            totalInvested = totalInvested.add(p.getTotalInvested());
            totalRealizedPnl = totalRealizedPnl.add(
                    p.getRealizedPnl() != null ? p.getRealizedPnl() : BigDecimal.ZERO);
        }

        BigDecimal totalUnrealizedPnl = totalCurrentValue.subtract(totalInvested);
        BigDecimal totalUnrealizedPnlPct = totalInvested.compareTo(BigDecimal.ZERO) != 0
                ? totalUnrealizedPnl.divide(totalInvested, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO;

        // Map holdings with allocation %
        final BigDecimal finalTotalValue = totalCurrentValue;
        List<PortfolioItemResponse> holdingResponses = holdings.stream()
                .map(p -> mapToItemResponse(p, finalTotalValue))
                .collect(Collectors.toList());

        Long totalTrades = orderRepository.countExecutedByUserId(user.getId());

        return PortfolioSummaryResponse.builder()
                .totalInvested(totalInvested.setScale(2, RoundingMode.HALF_UP))
                .totalCurrentValue(totalCurrentValue.setScale(2, RoundingMode.HALF_UP))
                .totalUnrealizedPnl(totalUnrealizedPnl.setScale(2, RoundingMode.HALF_UP))
                .totalUnrealizedPnlPct(totalUnrealizedPnlPct.setScale(2, RoundingMode.HALF_UP))
                .totalRealizedPnl(totalRealizedPnl.setScale(2, RoundingMode.HALF_UP))
                .cashBalance(user.getCashBalance().setScale(2, RoundingMode.HALF_UP))
                .totalPortfolioValue(user.getCashBalance().add(totalCurrentValue).setScale(2, RoundingMode.HALF_UP))
                .totalPositions((long) holdings.size())
                .totalTrades(totalTrades != null ? totalTrades : 0L)
                .holdings(holdingResponses)
                .build();
    }

    private PortfolioItemResponse mapToItemResponse(Portfolio p, BigDecimal totalValue) {
        BigDecimal allocationPct = BigDecimal.ZERO;
        if (totalValue.compareTo(BigDecimal.ZERO) > 0 && p.getCurrentValue() != null) {
            allocationPct = p.getCurrentValue()
                    .divide(totalValue, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        }

        return PortfolioItemResponse.builder()
                .id(p.getId())
                .symbol(p.getSymbol())
                .companyName(p.getCompanyName())
                .sector(p.getSector())
                .quantity(p.getQuantity())
                .averageCost(p.getAverageCost())
                .totalInvested(p.getTotalInvested())
                .currentPrice(p.getCurrentPrice())
                .currentValue(p.getCurrentValue())
                .unrealizedPnl(p.getUnrealizedPnl())
                .unrealizedPnlPct(p.getUnrealizedPnlPct())
                .realizedPnl(p.getRealizedPnl())
                .allocationPct(allocationPct.setScale(2, RoundingMode.HALF_UP))
                .createdAt(p.getCreatedAt())
                .build();
    }
}
