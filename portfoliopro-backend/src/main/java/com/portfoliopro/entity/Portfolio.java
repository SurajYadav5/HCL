package com.portfoliopro.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "portfolio", indexes = {
    @Index(name = "idx_portfolio_user_symbol", columnList = "user_id, symbol", unique = true)
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Portfolio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 20)
    private String symbol;

    @Column(name = "company_name", length = 100)
    private String companyName;

    @Column(name = "sector", length = 50)
    private String sector;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "average_cost", nullable = false, precision = 18, scale = 4)
    private BigDecimal averageCost; // Average cost basis per share

    @Column(name = "total_invested", nullable = false, precision = 18, scale = 2)
    private BigDecimal totalInvested; // Total amount invested

    @Column(name = "current_price", precision = 18, scale = 4)
    private BigDecimal currentPrice;

    @Column(name = "current_value", precision = 18, scale = 2)
    private BigDecimal currentValue; // Cached current market value

    @Column(name = "unrealized_pnl", precision = 18, scale = 2)
    private BigDecimal unrealizedPnl;

    @Column(name = "unrealized_pnl_pct", precision = 10, scale = 4)
    private BigDecimal unrealizedPnlPct;

    @Column(name = "realized_pnl", precision = 18, scale = 2)
    @Builder.Default
    private BigDecimal realizedPnl = BigDecimal.ZERO;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Recalculate derived fields based on current price.
     */
    public void refreshMetrics(BigDecimal latestPrice) {
        this.currentPrice = latestPrice;
        this.currentValue = latestPrice.multiply(BigDecimal.valueOf(quantity));
        this.unrealizedPnl = this.currentValue.subtract(this.totalInvested);
        if (totalInvested.compareTo(BigDecimal.ZERO) != 0) {
            this.unrealizedPnlPct = this.unrealizedPnl.divide(this.totalInvested, 6, java.math.RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        }
    }
}
