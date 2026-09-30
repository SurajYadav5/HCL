package com.portfoliopro.entity;

import com.portfoliopro.enums.OrderSide;
import com.portfoliopro.enums.OrderStatus;
import com.portfoliopro.enums.OrderType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders", indexes = {
    @Index(name = "idx_order_user", columnList = "user_id"),
    @Index(name = "idx_order_symbol", columnList = "symbol"),
    @Index(name = "idx_order_status", columnList = "status")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {

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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private OrderSide side; // BUY or SELL

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderType type; // MARKET or LIMIT

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status; // PENDING, EXECUTED, CANCELLED, FAILED

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "limit_price", precision = 18, scale = 4)
    private BigDecimal limitPrice; // null for MARKET orders

    @Column(name = "executed_price", precision = 18, scale = 4)
    private BigDecimal executedPrice;

    @Column(name = "total_value", precision = 18, scale = 2)
    private BigDecimal totalValue;

    @Column(name = "commission", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal commission = BigDecimal.ZERO;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "executed_at")
    private LocalDateTime executedAt;

    @Column(name = "notes", length = 500)
    private String notes;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
