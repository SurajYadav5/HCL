package com.portfoliopro.dto.response;

import com.portfoliopro.enums.OrderSide;
import com.portfoliopro.enums.OrderStatus;
import com.portfoliopro.enums.OrderType;
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
public class OrderResponse {
    private Long id;
    private String symbol;
    private String companyName;
    private OrderSide side;
    private OrderType type;
    private OrderStatus status;
    private Integer quantity;
    private BigDecimal limitPrice;
    private BigDecimal executedPrice;
    private BigDecimal totalValue;
    private BigDecimal commission;
    private LocalDateTime createdAt;
    private LocalDateTime executedAt;
    private String notes;
}
