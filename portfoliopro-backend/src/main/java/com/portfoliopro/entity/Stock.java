package com.portfoliopro.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "stocks", indexes = {
    @Index(name = "idx_stock_symbol", columnList = "symbol", unique = true),
    @Index(name = "idx_stock_sector", columnList = "sector")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Stock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String symbol;

    @Column(name = "company_name", nullable = false, length = 120)
    private String companyName;

    @Column(name = "sector", length = 60)
    private String sector;

    @Column(name = "price", precision = 18, scale = 4, nullable = false)
    private BigDecimal price;

    @Column(name = "open_price", precision = 18, scale = 4)
    private BigDecimal openPrice;

    @Column(name = "day_high", precision = 18, scale = 4)
    private BigDecimal dayHigh;

    @Column(name = "day_low", precision = 18, scale = 4)
    private BigDecimal dayLow;

    @Column(name = "previous_close", precision = 18, scale = 4)
    private BigDecimal previousClose;

    @Column(name = "change_amount", precision = 18, scale = 4)
    private BigDecimal changeAmount;

    @Column(name = "change_percent", precision = 10, scale = 4)
    private BigDecimal changePercent;

    @Column(name = "volume")
    private Long volume;

    @Column(name = "avg_volume")
    private Long avgVolume;

    @Column(name = "market_cap", precision = 24, scale = 2)
    private BigDecimal marketCap;

    @Column(name = "pe_ratio", precision = 10, scale = 2)
    private BigDecimal peRatio;

    @Column(name = "week52_high", precision = 18, scale = 4)
    private BigDecimal week52High;

    @Column(name = "week52_low", precision = 18, scale = 4)
    private BigDecimal week52Low;

    @Column(name = "exchange", length = 20)
    private String exchange;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    @PrePersist
    @PreUpdate
    protected void onSave() {
        if (lastUpdated == null) {
            lastUpdated = LocalDateTime.now();
        }
    }
}
