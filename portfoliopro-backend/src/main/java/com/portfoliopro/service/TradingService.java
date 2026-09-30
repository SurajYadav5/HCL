package com.portfoliopro.service;

import com.portfoliopro.dto.request.PlaceOrderRequest;
import com.portfoliopro.dto.response.OrderResponse;
import com.portfoliopro.dto.response.StockQuoteResponse;
import com.portfoliopro.entity.Order;
import com.portfoliopro.entity.Portfolio;
import com.portfoliopro.entity.User;
import com.portfoliopro.enums.OrderSide;
import com.portfoliopro.enums.OrderStatus;
import com.portfoliopro.enums.OrderType;
import com.portfoliopro.exception.BadRequestException;
import com.portfoliopro.exception.InsufficientFundsException;
import com.portfoliopro.exception.ResourceNotFoundException;
import com.portfoliopro.repository.OrderRepository;
import com.portfoliopro.repository.PortfolioRepository;
import com.portfoliopro.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TradingService {

    private static final BigDecimal COMMISSION_RATE = BigDecimal.valueOf(0.001); // 0.1% commission

    private final OrderRepository orderRepository;
    private final PortfolioRepository portfolioRepository;
    private final UserRepository userRepository;
    private final MarketDataService marketDataService;

    /**
     * Execute a trade order (BUY or SELL).
     * For MARKET orders: execute immediately at current market price.
     * For LIMIT orders: validate and place as pending (simplified: execute if price matches).
     */
    @Transactional
    public OrderResponse placeOrder(String username, PlaceOrderRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        // Validate LIMIT order has a price
        if (request.getType() == OrderType.LIMIT && request.getLimitPrice() == null) {
            throw new BadRequestException("Limit price is required for LIMIT orders");
        }

        // Get current market price
        StockQuoteResponse quote = marketDataService.getQuote(request.getSymbol().toUpperCase());
        BigDecimal marketPrice = quote.getPrice();
        BigDecimal executionPrice = request.getType() == OrderType.MARKET
                ? marketPrice
                : request.getLimitPrice();

        BigDecimal orderValue = executionPrice.multiply(BigDecimal.valueOf(request.getQuantity()));
        BigDecimal commission = orderValue.multiply(COMMISSION_RATE);
        BigDecimal totalCost = orderValue.add(commission);

        Order order = Order.builder()
                .user(user)
                .symbol(request.getSymbol().toUpperCase())
                .companyName(quote.getCompanyName())
                .side(request.getSide())
                .type(request.getType())
                .quantity(request.getQuantity())
                .limitPrice(request.getLimitPrice())
                .commission(commission)
                .status(OrderStatus.PENDING)
                .build();

        if (request.getSide() == OrderSide.BUY) {
            processBuyOrder(user, order, executionPrice, totalCost, quote.getCompanyName());
        } else {
            processSellOrder(user, order, executionPrice, totalCost, quote.getCompanyName());
        }

        order.setExecutedPrice(executionPrice);
        order.setTotalValue(orderValue);
        order.setStatus(OrderStatus.EXECUTED);
        order.setExecutedAt(LocalDateTime.now());

        userRepository.save(user);
        Order savedOrder = orderRepository.save(order);

        log.info("Order executed: {} {} {} shares of {} at ${}", 
                order.getSide(), order.getType(), order.getQuantity(), 
                order.getSymbol(), executionPrice);

        return mapToOrderResponse(savedOrder);
    }

    private void processBuyOrder(User user, Order order, BigDecimal price, BigDecimal totalCost, String companyName) {
        if (user.getCashBalance().compareTo(totalCost) < 0) {
            order.setStatus(OrderStatus.FAILED);
            order.setNotes("Insufficient funds. Required: $" + totalCost + ", Available: $" + user.getCashBalance());
            orderRepository.save(order);
            throw new InsufficientFundsException(
                "Insufficient funds. Required: $" + totalCost.setScale(2, java.math.RoundingMode.HALF_UP) +
                ", Available: $" + user.getCashBalance().setScale(2, java.math.RoundingMode.HALF_UP));
        }

        // Deduct cash
        user.setCashBalance(user.getCashBalance().subtract(totalCost));

        // Update portfolio
        Optional<Portfolio> existingPosition = portfolioRepository.findByUserIdAndSymbol(user.getId(), order.getSymbol());

        if (existingPosition.isPresent()) {
            Portfolio position = existingPosition.get();
            BigDecimal newTotalInvested = position.getTotalInvested()
                    .add(price.multiply(BigDecimal.valueOf(order.getQuantity())));
            int newQuantity = position.getQuantity() + order.getQuantity();
            BigDecimal newAvgCost = newTotalInvested.divide(
                    BigDecimal.valueOf(newQuantity), 4, java.math.RoundingMode.HALF_UP);

            position.setQuantity(newQuantity);
            position.setAverageCost(newAvgCost);
            position.setTotalInvested(newTotalInvested);
            position.refreshMetrics(price);
            portfolioRepository.save(position);
        } else {
            Portfolio newPosition = Portfolio.builder()
                    .user(user)
                    .symbol(order.getSymbol())
                    .companyName(companyName)
                    .quantity(order.getQuantity())
                    .averageCost(price)
                    .totalInvested(price.multiply(BigDecimal.valueOf(order.getQuantity())))
                    .realizedPnl(BigDecimal.ZERO)
                    .build();
            newPosition.refreshMetrics(price);
            portfolioRepository.save(newPosition);
        }
    }

    private void processSellOrder(User user, Order order, BigDecimal price, BigDecimal totalCost, String companyName) {
        Portfolio position = portfolioRepository.findByUserIdAndSymbol(user.getId(), order.getSymbol())
                .orElseThrow(() -> new BadRequestException(
                        "You don't hold any shares of " + order.getSymbol()));

        if (position.getQuantity() < order.getQuantity()) {
            throw new BadRequestException("Insufficient shares. You have " + position.getQuantity() +
                    " shares of " + order.getSymbol() + ", trying to sell " + order.getQuantity());
        }

        // Calculate realized P&L
        BigDecimal costBasis = position.getAverageCost().multiply(BigDecimal.valueOf(order.getQuantity()));
        BigDecimal saleProceeds = price.multiply(BigDecimal.valueOf(order.getQuantity()));
        BigDecimal realizedPnl = saleProceeds.subtract(costBasis).subtract(order.getCommission());

        // Add proceeds to cash (minus commission)
        user.setCashBalance(user.getCashBalance().add(saleProceeds).subtract(order.getCommission()));

        // Update or remove portfolio position
        int remainingQty = position.getQuantity() - order.getQuantity();
        if (remainingQty == 0) {
            portfolioRepository.delete(position);
        } else {
            position.setQuantity(remainingQty);
            BigDecimal remainingInvested = position.getAverageCost().multiply(BigDecimal.valueOf(remainingQty));
            position.setTotalInvested(remainingInvested);
            position.setRealizedPnl(position.getRealizedPnl().add(realizedPnl));
            position.refreshMetrics(price);
            portfolioRepository.save(position);
        }
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> getOrderHistory(String username, Pageable pageable) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
        return orderRepository.findByUserIdOrderByCreatedAtDesc(user.getId(), pageable)
                .map(this::mapToOrderResponse);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(String username, Long orderId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));
        if (!order.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("Access denied to this order");
        }
        return mapToOrderResponse(order);
    }

    private OrderResponse mapToOrderResponse(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .symbol(order.getSymbol())
                .companyName(order.getCompanyName())
                .side(order.getSide())
                .type(order.getType())
                .status(order.getStatus())
                .quantity(order.getQuantity())
                .limitPrice(order.getLimitPrice())
                .executedPrice(order.getExecutedPrice())
                .totalValue(order.getTotalValue())
                .commission(order.getCommission())
                .createdAt(order.getCreatedAt())
                .executedAt(order.getExecutedAt())
                .notes(order.getNotes())
                .build();
    }
}
