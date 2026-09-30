package com.portfoliopro.dto.request;

import com.portfoliopro.enums.OrderSide;
import com.portfoliopro.enums.OrderType;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PlaceOrderRequest {

    @NotBlank(message = "Symbol is required")
    @Size(max = 20)
    @Pattern(regexp = "^[A-Z]{1,5}$", message = "Symbol must be 1-5 uppercase letters")
    private String symbol;

    @NotNull(message = "Order side (BUY/SELL) is required")
    private OrderSide side;

    @NotNull(message = "Order type (MARKET/LIMIT) is required")
    private OrderType type;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    @Max(value = 100000, message = "Quantity cannot exceed 100,000")
    private Integer quantity;

    @DecimalMin(value = "0.01", message = "Limit price must be positive")
    private BigDecimal limitPrice; // Required for LIMIT orders
}
