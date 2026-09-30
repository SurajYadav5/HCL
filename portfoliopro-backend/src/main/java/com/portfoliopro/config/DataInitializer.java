package com.portfoliopro.config;

import com.portfoliopro.entity.Order;
import com.portfoliopro.entity.Portfolio;
import com.portfoliopro.entity.User;
import com.portfoliopro.entity.Watchlist;
import com.portfoliopro.enums.OrderSide;
import com.portfoliopro.enums.OrderStatus;
import com.portfoliopro.enums.OrderType;
import com.portfoliopro.enums.Role;
import com.portfoliopro.repository.OrderRepository;
import com.portfoliopro.repository.PortfolioRepository;
import com.portfoliopro.repository.UserRepository;
import com.portfoliopro.repository.WatchlistRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PortfolioRepository portfolioRepository;
    private final OrderRepository orderRepository;
    private final WatchlistRepository watchlistRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.findByUsername("trader_demo").isEmpty()) {
            log.info("Initializing demo trader account and paper trading portfolio...");

            User demoUser = User.builder()
                    .username("trader_demo")
                    .password(passwordEncoder.encode("Trader123!"))
                    .email("trader@portfoliopro.com")
                    .firstName("Demo")
                    .lastName("Trader")
                    .role(Role.USER)
                    .cashBalance(BigDecimal.valueOf(89455.00)) // $100k minus initial portfolio buys
                    .isActive(true)
                    .build();

            demoUser = userRepository.save(demoUser);

            // Seed holdings
            Portfolio aapl = Portfolio.builder()
                    .user(demoUser)
                    .symbol("AAPL")
                    .companyName("Apple Inc.")
                    .sector("Technology")
                    .quantity(25)
                    .averageCost(BigDecimal.valueOf(185.00))
                    .totalInvested(BigDecimal.valueOf(4625.00))
                    .currentPrice(BigDecimal.valueOf(189.84))
                    .currentValue(BigDecimal.valueOf(4746.00))
                    .unrealizedPnl(BigDecimal.valueOf(121.00))
                    .unrealizedPnlPct(BigDecimal.valueOf(2.6162))
                    .realizedPnl(BigDecimal.ZERO)
                    .build();

            Portfolio nvda = Portfolio.builder()
                    .user(demoUser)
                    .symbol("NVDA")
                    .companyName("NVIDIA Corp.")
                    .sector("Technology")
                    .quantity(15)
                    .averageCost(BigDecimal.valueOf(118.00))
                    .totalInvested(BigDecimal.valueOf(1770.00))
                    .currentPrice(BigDecimal.valueOf(128.50))
                    .currentValue(BigDecimal.valueOf(1927.50))
                    .unrealizedPnl(BigDecimal.valueOf(157.50))
                    .unrealizedPnlPct(BigDecimal.valueOf(8.8983))
                    .realizedPnl(BigDecimal.ZERO)
                    .build();

            Portfolio msft = Portfolio.builder()
                    .user(demoUser)
                    .symbol("MSFT")
                    .companyName("Microsoft Corp.")
                    .sector("Technology")
                    .quantity(10)
                    .averageCost(BigDecimal.valueOf(415.00))
                    .totalInvested(BigDecimal.valueOf(4150.00))
                    .currentPrice(BigDecimal.valueOf(428.74))
                    .currentValue(BigDecimal.valueOf(4287.40))
                    .unrealizedPnl(BigDecimal.valueOf(137.40))
                    .unrealizedPnlPct(BigDecimal.valueOf(3.3108))
                    .realizedPnl(BigDecimal.ZERO)
                    .build();

            portfolioRepository.save(aapl);
            portfolioRepository.save(nvda);
            portfolioRepository.save(msft);

            // Seed initial order execution logs
            Order order1 = Order.builder()
                    .user(demoUser)
                    .symbol("AAPL")
                    .companyName("Apple Inc.")
                    .side(OrderSide.BUY)
                    .type(OrderType.MARKET)
                    .status(OrderStatus.EXECUTED)
                    .quantity(25)
                    .executedPrice(BigDecimal.valueOf(185.00))
                    .totalValue(BigDecimal.valueOf(4625.00))
                    .commission(BigDecimal.ZERO)
                    .executedAt(LocalDateTime.now().minusDays(3))
                    .build();

            Order order2 = Order.builder()
                    .user(demoUser)
                    .symbol("NVDA")
                    .companyName("NVIDIA Corp.")
                    .side(OrderSide.BUY)
                    .type(OrderType.MARKET)
                    .status(OrderStatus.EXECUTED)
                    .quantity(15)
                    .executedPrice(BigDecimal.valueOf(118.00))
                    .totalValue(BigDecimal.valueOf(1770.00))
                    .commission(BigDecimal.ZERO)
                    .executedAt(LocalDateTime.now().minusDays(2))
                    .build();

            Order order3 = Order.builder()
                    .user(demoUser)
                    .symbol("MSFT")
                    .companyName("Microsoft Corp.")
                    .side(OrderSide.BUY)
                    .type(OrderType.LIMIT)
                    .limitPrice(BigDecimal.valueOf(415.00))
                    .status(OrderStatus.EXECUTED)
                    .quantity(10)
                    .executedPrice(BigDecimal.valueOf(415.00))
                    .totalValue(BigDecimal.valueOf(4150.00))
                    .commission(BigDecimal.ZERO)
                    .executedAt(LocalDateTime.now().minusDays(1))
                    .build();

            orderRepository.save(order1);
            orderRepository.save(order2);
            orderRepository.save(order3);

            // Seed watchlist items
            watchlistRepository.save(Watchlist.builder().user(demoUser).symbol("TSLA").companyName("Tesla Inc.").build());
            watchlistRepository.save(Watchlist.builder().user(demoUser).symbol("AMZN").companyName("Amazon.com Inc.").build());
            watchlistRepository.save(Watchlist.builder().user(demoUser).symbol("GOOGL").companyName("Alphabet Inc.").build());

            log.info("Demo trader account initialization complete. Username: trader_demo / Password: Trader123!");
        }
    }
}
