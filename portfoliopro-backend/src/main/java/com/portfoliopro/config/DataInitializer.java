package com.portfoliopro.config;

import com.portfoliopro.entity.Order;
import com.portfoliopro.entity.Portfolio;
import com.portfoliopro.entity.Stock;
import com.portfoliopro.entity.User;
import com.portfoliopro.entity.Watchlist;
import com.portfoliopro.enums.OrderSide;
import com.portfoliopro.enums.OrderStatus;
import com.portfoliopro.enums.OrderType;
import com.portfoliopro.enums.Role;
import com.portfoliopro.repository.OrderRepository;
import com.portfoliopro.repository.PortfolioRepository;
import com.portfoliopro.repository.StockRepository;
import com.portfoliopro.repository.UserRepository;
import com.portfoliopro.repository.WatchlistRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PortfolioRepository portfolioRepository;
    private final OrderRepository orderRepository;
    private final WatchlistRepository watchlistRepository;
    private final StockRepository stockRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        log.info("Checking PortfolioPro institutional database seed data...");

        User demoUser = getOrCreateDemoUser();
        seedStocksCatalog();
        seedPortfolioHoldings(demoUser);
        seedHistoricalOrders(demoUser);
        seedWatchlist(demoUser);

        log.info("Database initialization verified! Total Stocks: {}, Total Orders: {}, Total Holdings: {}",
                stockRepository.count(), orderRepository.count(), portfolioRepository.count());
    }

    private User getOrCreateDemoUser() {
        return userRepository.findByUsername("trader_demo").orElseGet(() -> {
            log.info("Creating demo trader account...");
            User user = User.builder()
                    .username("trader_demo")
                    .password(passwordEncoder.encode("Trader123!"))
                    .email("trader@portfoliopro.com")
                    .firstName("Demo")
                    .lastName("Trader")
                    .role(Role.USER)
                    .cashBalance(BigDecimal.valueOf(74850.00))
                    .isActive(true)
                    .build();
            return userRepository.save(user);
        });
    }

    private void seedStocksCatalog() {
        if (stockRepository.count() >= 100) {
            log.info("Stock catalog already contains {} entries. Skipping seed.", stockRepository.count());
            return;
        }

        log.info("Seeding 105 institutional equities into the stock catalog...");
        List<Stock> stocks = new ArrayList<>();

        // 1. TECHNOLOGY (22)
        stocks.add(buildStock("AAPL", "Apple Inc.", "Technology", 189.84, 187.68, 2950000000000L, 31.4, 199.62, 164.08, "NASDAQ", 54200000L));
        stocks.add(buildStock("MSFT", "Microsoft Corp.", "Technology", 428.74, 425.22, 3180000000000L, 36.8, 468.35, 309.45, "NASDAQ", 22100000L));
        stocks.add(buildStock("NVDA", "NVIDIA Corp.", "Technology", 128.50, 125.60, 3150000000000L, 68.2, 140.76, 40.50, "NASDAQ", 78900000L));
        stocks.add(buildStock("AVGO", "Broadcom Inc.", "Technology", 162.30, 159.50, 755000000000L, 48.6, 185.16, 80.20, "NASDAQ", 18500000L));
        stocks.add(buildStock("ORCL", "Oracle Corp.", "Technology", 171.20, 169.60, 470000000000L, 41.2, 175.80, 99.26, "NYSE", 12400000L));
        stocks.add(buildStock("CRM", "Salesforce Inc.", "Technology", 298.50, 299.80, 288000000000L, 52.4, 318.71, 193.68, "NYSE", 6200000L));
        stocks.add(buildStock("AMD", "Advanced Micro Devices", "Technology", 156.40, 151.70, 252000000000L, 112.5, 227.30, 94.04, "NASDAQ", 48500000L));
        stocks.add(buildStock("CSCO", "Cisco Systems", "Technology", 52.40, 52.20, 210000000000L, 20.8, 58.19, 44.50, "NASDAQ", 17800000L));
        stocks.add(buildStock("QCOM", "Qualcomm Inc.", "Technology", 168.90, 166.50, 188000000000L, 22.4, 230.63, 104.33, "NASDAQ", 11200000L));
        stocks.add(buildStock("ADBE", "Adobe Inc.", "Technology", 512.60, 516.70, 229000000000L, 43.1, 638.25, 433.97, "NASDAQ", 3400000L));
        stocks.add(buildStock("TXN", "Texas Instruments", "Technology", 194.20, 192.90, 176000000000L, 34.2, 211.08, 139.48, "NASDAQ", 5800000L));
        stocks.add(buildStock("IBM", "International Business Machines", "Technology", 218.40, 216.00, 201000000000L, 24.5, 221.74, 137.35, "NYSE", 4900000L));
        stocks.add(buildStock("NOW", "ServiceNow Inc.", "Technology", 895.30, 876.40, 184000000000L, 78.4, 915.20, 527.24, "NYSE", 1900000L));
        stocks.add(buildStock("INTU", "Intuit Inc.", "Technology", 642.10, 637.30, 179000000000L, 62.1, 676.62, 477.58, "NASDAQ", 1800000L));
        stocks.add(buildStock("AMAT", "Applied Materials", "Technology", 198.50, 193.80, 164000000000L, 22.8, 255.89, 138.30, "NASDAQ", 8200000L));
        stocks.add(buildStock("MU", "Micron Technology", "Technology", 104.20, 102.30, 115000000000L, 46.2, 157.50, 64.75, "NASDAQ", 24500000L));
        stocks.add(buildStock("PANW", "Palo Alto Networks", "Technology", 352.00, 347.80, 114000000000L, 48.9, 380.84, 243.60, "NASDAQ", 3100000L));
        stocks.add(buildStock("SNPS", "Synopsys Inc.", "Technology", 518.70, 514.10, 79000000000L, 54.3, 629.38, 439.00, "NASDAQ", 1200000L));
        stocks.add(buildStock("CRWD", "CrowdStrike Holdings", "Technology", 295.40, 285.70, 72000000000L, 88.4, 398.33, 200.81, "NASDAQ", 6400000L));
        stocks.add(buildStock("INTC", "Intel Corp.", "Technology", 22.80, 23.10, 97000000000L, 16.5, 51.28, 18.51, "NASDAQ", 64200000L));
        stocks.add(buildStock("KLAC", "KLA Corp.", "Technology", 714.20, 702.50, 96000000000L, 34.2, 896.32, 447.11, "NASDAQ", 1600000L));
        stocks.add(buildStock("CDNS", "Cadence Design Systems", "Technology", 274.60, 271.80, 75000000000L, 71.4, 331.84, 224.20, "NASDAQ", 1800000L));

        // 2. FINANCIAL SERVICES (15)
        stocks.add(buildStock("JPM", "JPMorgan Chase & Co.", "Financial Services", 215.40, 214.20, 615000000000L, 12.1, 225.48, 140.33, "NYSE", 8900000L));
        stocks.add(buildStock("V", "Visa Inc.", "Financial Services", 278.50, 276.80, 565000000000L, 30.2, 290.96, 227.68, "NYSE", 5800000L));
        stocks.add(buildStock("MA", "Mastercard Inc.", "Financial Services", 485.20, 481.70, 448000000000L, 35.8, 495.80, 368.10, "NYSE", 2800000L));
        stocks.add(buildStock("BAC", "Bank of America Corp.", "Financial Services", 39.80, 39.60, 308000000000L, 13.4, 44.44, 24.96, "NYSE", 32500000L));
        stocks.add(buildStock("WFC", "Wells Fargo & Co.", "Financial Services", 56.20, 56.40, 196000000000L, 11.8, 62.55, 38.60, "NYSE", 14200000L));
        stocks.add(buildStock("GS", "Goldman Sachs Group", "Financial Services", 482.30, 475.60, 158000000000L, 15.6, 514.88, 289.36, "NYSE", 2400000L));
        stocks.add(buildStock("MS", "Morgan Stanley", "Financial Services", 102.50, 101.50, 166000000000L, 17.2, 107.50, 69.42, "NYSE", 7800000L));
        stocks.add(buildStock("BLK", "BlackRock Inc.", "Financial Services", 890.00, 880.00, 132000000000L, 21.8, 925.00, 622.00, "NYSE", 850000L));
        stocks.add(buildStock("C", "Citigroup Inc.", "Financial Services", 61.70, 61.20, 117000000000L, 15.2, 67.81, 38.17, "NYSE", 12500000L));
        stocks.add(buildStock("AXP", "American Express Co.", "Financial Services", 264.10, 260.70, 188000000000L, 20.4, 270.50, 140.91, "NYSE", 2600000L));
        stocks.add(buildStock("SCHW", "Charles Schwab Corp.", "Financial Services", 64.80, 64.90, 118000000000L, 26.5, 79.49, 49.30, "NYSE", 9800000L));
        stocks.add(buildStock("CB", "Chubb Limited", "Financial Services", 284.60, 283.50, 115000000000L, 12.4, 292.00, 197.80, "NYSE", 1600000L));
        stocks.add(buildStock("PGR", "Progressive Corp.", "Financial Services", 248.10, 245.50, 145000000000L, 20.2, 256.00, 145.20, "NYSE", 2100000L));
        stocks.add(buildStock("MMC", "Marsh & McLennan", "Financial Services", 224.80, 223.70, 111000000000L, 28.6, 230.15, 178.60, "NYSE", 1400000L));
        stocks.add(buildStock("CME", "CME Group Inc.", "Financial Services", 218.90, 218.10, 78000000000L, 23.4, 225.40, 182.50, "NASDAQ", 1500000L));

        // 3. HEALTHCARE (14)
        stocks.add(buildStock("LLY", "Eli Lilly and Co.", "Healthcare", 884.20, 866.00, 840000000000L, 118.5, 972.53, 516.57, "NYSE", 3200000L));
        stocks.add(buildStock("UNH", "UnitedHealth Group", "Healthcare", 582.40, 579.80, 536000000000L, 28.4, 606.37, 436.38, "NYSE", 2900000L));
        stocks.add(buildStock("JNJ", "Johnson & Johnson", "Healthcare", 162.80, 163.20, 391000000000L, 24.8, 168.96, 143.16, "NYSE", 6800000L));
        stocks.add(buildStock("ABBV", "AbbVie Inc.", "Healthcare", 194.50, 193.20, 343000000000L, 48.2, 199.98, 135.00, "NYSE", 4700000L));
        stocks.add(buildStock("MRK", "Merck & Co.", "Healthcare", 114.30, 114.80, 289000000000L, 21.6, 134.63, 98.40, "NYSE", 7400000L));
        stocks.add(buildStock("TMO", "Thermo Fisher Scientific", "Healthcare", 592.10, 587.40, 226000000000L, 35.8, 610.00, 442.20, "NYSE", 1300000L));
        stocks.add(buildStock("ABT", "Abbott Laboratories", "Healthcare", 112.40, 112.10, 195000000000L, 34.6, 121.64, 89.24, "NYSE", 4600000L));
        stocks.add(buildStock("DHR", "Danaher Corp.", "Healthcare", 274.50, 271.90, 203000000000L, 44.8, 282.80, 182.00, "NYSE", 2200000L));
        stocks.add(buildStock("AMGN", "Amgen Inc.", "Healthcare", 318.20, 314.60, 170000000000L, 41.2, 346.85, 252.30, "NASDAQ", 1800000L));
        stocks.add(buildStock("ISRG", "Intuitive Surgical", "Healthcare", 488.60, 476.90, 172000000000L, 78.4, 503.70, 254.00, "NASDAQ", 1700000L));
        stocks.add(buildStock("PFE", "Pfizer Inc.", "Healthcare", 28.60, 28.80, 162000000000L, 18.2, 34.36, 24.48, "NYSE", 28900000L));
        stocks.add(buildStock("BMY", "Bristol-Myers Squibb", "Healthcare", 49.80, 50.10, 101000000000L, 16.4, 59.80, 39.30, "NYSE", 9800000L));
        stocks.add(buildStock("GILD", "Gilead Sciences", "Healthcare", 83.40, 82.90, 103000000000L, 15.8, 87.20, 62.40, "NASDAQ", 5400000L));
        stocks.add(buildStock("VRTX", "Vertex Pharmaceuticals", "Healthcare", 472.10, 465.80, 121000000000L, 38.6, 510.64, 340.00, "NASDAQ", 1200000L));

        // 4. CONSUMER CYCLICAL (12)
        stocks.add(buildStock("AMZN", "Amazon.com Inc.", "Consumer Cyclical", 186.40, 183.92, 1940000000000L, 42.5, 201.20, 118.35, "NASDAQ", 34100000L));
        stocks.add(buildStock("TSLA", "Tesla Inc.", "Consumer Cyclical", 254.30, 259.80, 810000000000L, 64.9, 271.00, 138.80, "NASDAQ", 68300000L));
        stocks.add(buildStock("HD", "Home Depot Inc.", "Consumer Cyclical", 398.20, 394.80, 395000000000L, 26.2, 415.00, 274.20, "NYSE", 3400000L));
        stocks.add(buildStock("MCD", "McDonald's Corp.", "Consumer Cyclical", 298.40, 297.20, 214000000000L, 25.1, 303.40, 243.50, "NYSE", 2600000L));
        stocks.add(buildStock("NKE", "Nike Inc.", "Consumer Cyclical", 88.50, 89.50, 132000000000L, 28.4, 123.39, 70.75, "NYSE", 8900000L));
        stocks.add(buildStock("SBUX", "Starbucks Corp.", "Consumer Cyclical", 96.20, 95.50, 109000000000L, 27.8, 107.66, 71.55, "NASDAQ", 7800000L));
        stocks.add(buildStock("LOW", "Lowe's Companies", "Consumer Cyclical", 264.80, 263.10, 150000000000L, 21.6, 275.40, 181.80, "NYSE", 2400000L));
        stocks.add(buildStock("BKNG", "Booking Holdings", "Consumer Cyclical", 4120.00, 4047.00, 138000000000L, 31.4, 4250.00, 2750.00, "NASDAQ", 340000L));
        stocks.add(buildStock("TJX", "TJX Companies", "Consumer Cyclical", 116.40, 115.80, 131000000000L, 28.5, 120.80, 84.60, "NYSE", 4100000L));
        stocks.add(buildStock("F", "Ford Motor Co.", "Consumer Cyclical", 10.80, 10.85, 43000000000L, 11.2, 14.85, 9.49, "NYSE", 48000000L));
        stocks.add(buildStock("GM", "General Motors Co.", "Consumer Cyclical", 46.20, 45.80, 52000000000L, 5.8, 50.50, 26.30, "NYSE", 12500000L));
        stocks.add(buildStock("LULU", "Lululemon Athletica", "Consumer Cyclical", 284.10, 280.20, 35000000000L, 21.8, 516.29, 242.00, "NASDAQ", 2200000L));

        // 5. COMMUNICATION SERVICES (10)
        stocks.add(buildStock("GOOGL", "Alphabet Inc. (Class A)", "Communication Services", 177.30, 178.40, 2210000000000L, 25.1, 191.75, 120.21, "NASDAQ", 18400000L));
        stocks.add(buildStock("META", "Meta Platforms Inc.", "Communication Services", 584.60, 574.00, 1480000000000L, 27.8, 602.95, 279.40, "NASDAQ", 14200000L));
        stocks.add(buildStock("NFLX", "Netflix Inc.", "Communication Services", 712.40, 697.10, 306000000000L, 42.6, 736.00, 344.70, "NASDAQ", 3600000L));
        stocks.add(buildStock("DIS", "Walt Disney Co.", "Communication Services", 96.80, 96.30, 176000000000L, 41.5, 123.74, 78.73, "NYSE", 9800000L));
        stocks.add(buildStock("CMCSA", "Comcast Corp.", "Communication Services", 42.10, 42.25, 162000000000L, 11.4, 47.46, 36.43, "NASDAQ", 16500000L));
        stocks.add(buildStock("TMUS", "T-Mobile US", "Communication Services", 204.50, 202.30, 238000000000L, 26.2, 212.00, 138.40, "NASDAQ", 4200000L));
        stocks.add(buildStock("VZ", "Verizon Communications", "Communication Services", 44.20, 44.10, 186000000000L, 15.6, 45.36, 30.14, "NYSE", 18200000L));
        stocks.add(buildStock("T", "AT&T Inc.", "Communication Services", 21.80, 21.67, 156000000000L, 11.2, 22.34, 14.12, "NYSE", 29400000L));
        stocks.add(buildStock("SPOT", "Spotify Technology", "Communication Services", 378.20, 366.80, 75000000000L, 95.4, 398.00, 145.00, "NYSE", 1900000L));
        stocks.add(buildStock("EA", "Electronic Arts", "Communication Services", 146.50, 145.80, 39000000000L, 34.8, 153.70, 118.90, "NASDAQ", 1800000L));

        // 6. CONSUMER DEFENSIVE (9)
        stocks.add(buildStock("WMT", "Walmart Inc.", "Consumer Defensive", 79.80, 79.05, 642000000000L, 33.4, 82.20, 49.85, "NYSE", 18900000L));
        stocks.add(buildStock("COST", "Costco Wholesale", "Consumer Defensive", 902.40, 890.80, 400000000000L, 54.2, 923.83, 538.00, "NASDAQ", 1800000L));
        stocks.add(buildStock("PG", "Procter & Gamble", "Consumer Defensive", 172.50, 172.00, 404000000000L, 28.2, 177.94, 141.45, "NYSE", 5800000L));
        stocks.add(buildStock("KO", "Coca-Cola Co.", "Consumer Defensive", 71.20, 70.90, 306000000000L, 28.1, 73.53, 51.55, "NYSE", 11200000L));
        stocks.add(buildStock("PEP", "PepsiCo Inc.", "Consumer Defensive", 172.80, 173.15, 237000000000L, 25.4, 183.17, 155.83, "NASDAQ", 4400000L));
        stocks.add(buildStock("PM", "Philip Morris International", "Consumer Defensive", 122.40, 121.40, 190000000000L, 21.6, 128.50, 87.10, "NYSE", 3800000L));
        stocks.add(buildStock("MDLZ", "Mondelez International", "Consumer Defensive", 72.10, 72.00, 97000000000L, 24.5, 77.20, 64.90, "NASDAQ", 5900000L));
        stocks.add(buildStock("CL", "Colgate-Palmolive", "Consumer Defensive", 102.50, 102.00, 84000000000L, 31.8, 106.80, 70.20, "NYSE", 3600000L));
        stocks.add(buildStock("TGT", "Target Corp.", "Consumer Defensive", 154.20, 155.40, 71000000000L, 16.8, 181.86, 102.93, "NYSE", 4100000L));

        // 7. ENERGY (8)
        stocks.add(buildStock("XOM", "Exxon Mobil Corp.", "Energy", 118.40, 117.00, 526000000000L, 14.6, 123.75, 95.77, "NYSE", 14500000L));
        stocks.add(buildStock("CVX", "Chevron Corp.", "Energy", 148.60, 147.20, 272000000000L, 14.2, 167.11, 137.50, "NYSE", 6800000L));
        stocks.add(buildStock("COP", "ConocoPhillips", "Energy", 108.20, 107.10, 126000000000L, 12.8, 134.40, 101.80, "NYSE", 5400000L));
        stocks.add(buildStock("SLB", "Schlumberger Ltd.", "Energy", 44.50, 44.20, 63000000000L, 14.5, 62.12, 41.80, "NYSE", 8900000L));
        stocks.add(buildStock("EOG", "EOG Resources", "Energy", 126.80, 125.40, 72000000000L, 10.8, 139.50, 108.90, "NYSE", 2800000L));
        stocks.add(buildStock("MPC", "Marathon Petroleum", "Energy", 168.40, 166.10, 58000000000L, 9.4, 221.11, 140.20, "NYSE", 3100000L));
        stocks.add(buildStock("PSX", "Phillips 66", "Energy", 134.20, 133.10, 56000000000L, 12.1, 169.50, 113.80, "NYSE", 2400000L));
        stocks.add(buildStock("OXY", "Occidental Petroleum", "Energy", 52.60, 52.30, 47000000000L, 13.8, 71.18, 50.10, "NYSE", 8600000L));

        // 8. INDUSTRIALS (9)
        stocks.add(buildStock("GE", "GE Aerospace", "Industrials", 188.40, 185.30, 204000000000L, 38.6, 194.50, 84.20, "NYSE", 5200000L));
        stocks.add(buildStock("CAT", "Caterpillar Inc.", "Industrials", 392.50, 387.50, 191000000000L, 18.2, 401.80, 223.70, "NYSE", 2800000L));
        stocks.add(buildStock("UNP", "Union Pacific Corp.", "Industrials", 244.20, 243.10, 148000000000L, 22.8, 258.80, 195.40, "NYSE", 2100000L));
        stocks.add(buildStock("HON", "Honeywell International", "Industrials", 208.60, 207.10, 135000000000L, 24.1, 218.40, 174.90, "NASDAQ", 2400000L));
        stocks.add(buildStock("RTX", "RTX Corp.", "Industrials", 122.50, 121.20, 162000000000L, 42.5, 126.80, 68.50, "NYSE", 4800000L));
        stocks.add(buildStock("LMT", "Lockheed Martin", "Industrials", 582.40, 577.20, 139000000000L, 21.4, 614.50, 393.80, "NYSE", 1100000L));
        stocks.add(buildStock("DE", "Deere & Co.", "Industrials", 412.00, 408.70, 114000000000L, 14.8, 417.90, 345.20, "NYSE", 1200000L));
        stocks.add(buildStock("UPS", "United Parcel Service", "Industrials", 134.50, 135.00, 114000000000L, 21.2, 163.50, 123.10, "NYSE", 3900000L));
        stocks.add(buildStock("BA", "Boeing Co.", "Industrials", 154.80, 157.60, 95000000000L, 34.5, 267.54, 149.20, "NYSE", 7800000L));

        // 9. REAL ESTATE & UTILITIES (8)
        stocks.add(buildStock("PLD", "Prologis Inc.", "Real Estate", 124.80, 124.20, 115000000000L, 36.4, 137.52, 96.63, "NYSE", 3200000L));
        stocks.add(buildStock("AMT", "American Tower", "Real Estate", 228.40, 226.70, 106000000000L, 44.8, 241.00, 154.60, "NYSE", 1900000L));
        stocks.add(buildStock("EQIX", "Equinix Inc.", "Real Estate", 878.20, 865.60, 84000000000L, 88.2, 913.00, 685.00, "NASDAQ", 490000L));
        stocks.add(buildStock("SPG", "Simon Property Group", "Real Estate", 168.50, 167.40, 55000000000L, 21.6, 174.20, 108.60, "NYSE", 1600000L));
        stocks.add(buildStock("NEE", "NextEra Energy", "Utilities", 84.20, 83.45, 173000000000L, 24.5, 85.90, 47.25, "NYSE", 8900000L));
        stocks.add(buildStock("SO", "Southern Co.", "Utilities", 90.50, 90.14, 99000000000L, 21.8, 92.40, 64.20, "NYSE", 3800000L));
        stocks.add(buildStock("DUK", "Duke Energy", "Utilities", 114.80, 114.40, 88000000000L, 19.5, 117.80, 86.40, "NYSE", 2900000L));
        stocks.add(buildStock("CEG", "Constellation Energy", "Utilities", 268.40, 258.60, 84000000000L, 38.4, 282.00, 108.30, "NASDAQ", 3600000L));

        stockRepository.saveAll(stocks);
        log.info("Successfully seeded {} institutional stocks!", stocks.size());
    }

    private Stock buildStock(String symbol, String name, String sector, double price, double prev,
                             long cap, double pe, double h52, double l52, String ex, long vol) {
        BigDecimal p = BigDecimal.valueOf(price);
        BigDecimal pr = BigDecimal.valueOf(prev);
        BigDecimal ch = p.subtract(pr);
        BigDecimal chPct = ch.divide(pr, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));

        return Stock.builder()
                .symbol(symbol)
                .companyName(name)
                .sector(sector)
                .price(p)
                .previousClose(pr)
                .openPrice(pr.add(ch.multiply(BigDecimal.valueOf(0.4))))
                .dayHigh(p.max(pr).add(BigDecimal.valueOf(price * 0.015)).setScale(2, RoundingMode.HALF_UP))
                .dayLow(p.min(pr).subtract(BigDecimal.valueOf(price * 0.015)).setScale(2, RoundingMode.HALF_UP))
                .changeAmount(ch)
                .changePercent(chPct)
                .marketCap(BigDecimal.valueOf(cap))
                .peRatio(BigDecimal.valueOf(pe))
                .week52High(BigDecimal.valueOf(h52))
                .week52Low(BigDecimal.valueOf(l52))
                .exchange(ex)
                .volume(vol)
                .avgVolume(vol + 2000000L)
                .lastUpdated(LocalDateTime.now())
                .build();
    }

    private void seedPortfolioHoldings(User user) {
        if (portfolioRepository.findByUserId(user.getId()).size() >= 10) {
            return;
        }

        portfolioRepository.deleteAll(portfolioRepository.findByUserId(user.getId()));
        log.info("Seeding 16 diversified portfolio holdings...");

        List<Portfolio> holdings = List.of(
                createHolding(user, "AAPL", "Apple Inc.", "Technology", 40, 181.50, 189.84),
                createHolding(user, "NVDA", "NVIDIA Corp.", "Technology", 50, 114.20, 128.50),
                createHolding(user, "MSFT", "Microsoft Corp.", "Technology", 20, 410.00, 428.74),
                createHolding(user, "AMZN", "Amazon.com Inc.", "Consumer Cyclical", 35, 178.40, 186.40),
                createHolding(user, "GOOGL", "Alphabet Inc.", "Communication Services", 25, 172.00, 177.30),
                createHolding(user, "META", "Meta Platforms Inc.", "Communication Services", 15, 520.00, 584.60),
                createHolding(user, "JPM", "JPMorgan Chase & Co.", "Financial Services", 30, 204.00, 215.40),
                createHolding(user, "LLY", "Eli Lilly and Co.", "Healthcare", 10, 840.00, 884.20),
                createHolding(user, "XOM", "Exxon Mobil Corp.", "Energy", 45, 112.50, 118.40),
                createHolding(user, "WMT", "Walmart Inc.", "Consumer Defensive", 50, 74.20, 79.80),
                createHolding(user, "COST", "Costco Wholesale", "Consumer Defensive", 8, 860.00, 902.40),
                createHolding(user, "AMD", "Advanced Micro Devices", "Technology", 30, 142.00, 156.40),
                createHolding(user, "UNH", "UnitedHealth Group", "Healthcare", 12, 545.00, 582.40),
                createHolding(user, "GE", "GE Aerospace", "Industrials", 25, 168.00, 188.40),
                createHolding(user, "TSLA", "Tesla Inc.", "Consumer Cyclical", 20, 260.00, 254.30),
                createHolding(user, "NEE", "NextEra Energy", "Utilities", 50, 78.50, 84.20)
        );

        portfolioRepository.saveAll(holdings);
    }

    private Portfolio createHolding(User user, String sym, String name, String sector, int qty, double avgCost, double curPrice) {
        BigDecimal avg = BigDecimal.valueOf(avgCost);
        BigDecimal cur = BigDecimal.valueOf(curPrice);
        BigDecimal invested = avg.multiply(BigDecimal.valueOf(qty));
        BigDecimal value = cur.multiply(BigDecimal.valueOf(qty));
        BigDecimal pnl = value.subtract(invested);
        BigDecimal pnlPct = pnl.divide(invested, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));

        return Portfolio.builder()
                .user(user)
                .symbol(sym)
                .companyName(name)
                .sector(sector)
                .quantity(qty)
                .averageCost(avg)
                .totalInvested(invested)
                .currentPrice(cur)
                .currentValue(value)
                .unrealizedPnl(pnl)
                .unrealizedPnlPct(pnlPct)
                .realizedPnl(BigDecimal.valueOf(qty * 3.5))
                .build();
    }

    private void seedHistoricalOrders(User user) {
        if (orderRepository.count() >= 100) {
            log.info("Order repository already has {} orders. Skipping seed.", orderRepository.count());
            return;
        }

        log.info("Seeding 105 realistic historical trade audit executions...");
        List<Order> orders = new ArrayList<>();

        String[] symbols = {
            "AAPL", "NVDA", "MSFT", "AMZN", "GOOGL", "TSLA", "META", "JPM", "LLY", "UNH",
            "XOM", "V", "JNJ", "WMT", "COST", "AMD", "NFLX", "ORCL", "CRM", "AVGO",
            "HD", "MCD", "NKE", "DIS", "QCOM", "CAT", "GE", "BA", "BAC", "GS", "CEG", "SPOT"
        };

        String[] companies = {
            "Apple Inc.", "NVIDIA Corp.", "Microsoft Corp.", "Amazon.com Inc.", "Alphabet Inc.",
            "Tesla Inc.", "Meta Platforms Inc.", "JPMorgan Chase", "Eli Lilly", "UnitedHealth Group",
            "Exxon Mobil", "Visa Inc.", "Johnson & Johnson", "Walmart Inc.", "Costco Wholesale",
            "Advanced Micro Devices", "Netflix Inc.", "Oracle Corp.", "Salesforce Inc.", "Broadcom Inc.",
            "Home Depot", "McDonald's", "Nike Inc.", "Walt Disney", "Qualcomm Inc.", "Caterpillar Inc.",
            "GE Aerospace", "Boeing Co.", "Bank of America", "Goldman Sachs", "Constellation Energy", "Spotify Tech"
        };

        double[] basePrices = {
            185.0, 118.0, 415.0, 180.0, 172.0, 250.0, 530.0, 208.0, 850.0, 560.0,
            114.0, 272.0, 160.0, 76.0, 875.0, 145.0, 680.0, 165.0, 290.0, 158.0,
            390.0, 292.0, 86.0, 94.0, 162.0, 380.0, 175.0, 160.0, 38.0, 470.0, 255.0, 365.0
        };

        LocalDateTime baseDate = LocalDateTime.now().minusDays(105);

        for (int i = 0; i < 105; i++) {
            int tickerIdx = i % symbols.length;
            String sym = symbols[tickerIdx];
            String company = companies[tickerIdx];
            double basePrice = basePrices[tickerIdx];

            // Mix sides: ~65% BUY, 35% SELL
            OrderSide side = (i % 3 == 0) ? OrderSide.SELL : OrderSide.BUY;
            // Mix types: ~80% MARKET, 20% LIMIT
            OrderType type = (i % 5 == 0) ? OrderType.LIMIT : OrderType.MARKET;
            
            // Mix statuses: mostly EXECUTED, with a few PENDING and CANCELLED
            OrderStatus status = OrderStatus.EXECUTED;
            if (i == 103 || i == 104) {
                status = OrderStatus.PENDING;
            } else if (i == 42 || i == 87) {
                status = OrderStatus.CANCELLED;
            }

            int qty = 5 + ((i * 7) % 45); // Between 5 and 50 shares
            double variance = ((i % 11) - 5) * 1.8;
            BigDecimal execPrice = BigDecimal.valueOf(Math.max(10.0, basePrice + variance)).setScale(2, RoundingMode.HALF_UP);
            BigDecimal totalVal = execPrice.multiply(BigDecimal.valueOf(qty));
            BigDecimal comm = BigDecimal.ZERO; // 0 commission broker
            LocalDateTime tradeTime = baseDate.plusDays(i).plusHours((i * 3) % 7).plusMinutes((i * 13) % 55);

            String routeNote = (i % 2 == 0)
                    ? "Direct NASDAQ DMA fill, latency 1.2ms"
                    : "NYSE Arca smart execution, 0 slippage";

            Order order = Order.builder()
                    .user(user)
                    .symbol(sym)
                    .companyName(company)
                    .side(side)
                    .type(type)
                    .status(status)
                    .quantity(qty)
                    .limitPrice(type == OrderType.LIMIT ? execPrice : null)
                    .executedPrice(status != OrderStatus.PENDING ? execPrice : null)
                    .totalValue(totalVal)
                    .commission(comm)
                    .createdAt(tradeTime)
                    .executedAt(status == OrderStatus.EXECUTED ? tradeTime.plusSeconds(3) : null)
                    .notes(status == OrderStatus.CANCELLED ? "Cancelled by user" : routeNote)
                    .build();

            orders.add(order);
        }

        orderRepository.saveAll(orders);
        log.info("Successfully seeded 105 historical trade audit records!");
    }

    private void seedWatchlist(User user) {
        if (watchlistRepository.count() >= 10) {
            return;
        }

        watchlistRepository.deleteAll(watchlistRepository.findByUserId(user.getId()));
        log.info("Seeding 12 watchlist items...");

        List<Watchlist> items = List.of(
                Watchlist.builder().user(user).symbol("TSLA").companyName("Tesla Inc.").build(),
                Watchlist.builder().user(user).symbol("NFLX").companyName("Netflix Inc.").build(),
                Watchlist.builder().user(user).symbol("AMD").companyName("Advanced Micro Devices").build(),
                Watchlist.builder().user(user).symbol("CRM").companyName("Salesforce Inc.").build(),
                Watchlist.builder().user(user).symbol("DIS").companyName("Walt Disney Co.").build(),
                Watchlist.builder().user(user).symbol("AVGO").companyName("Broadcom Inc.").build(),
                Watchlist.builder().user(user).symbol("CRWD").companyName("CrowdStrike Holdings").build(),
                Watchlist.builder().user(user).symbol("SPOT").companyName("Spotify Technology").build(),
                Watchlist.builder().user(user).symbol("BA").companyName("Boeing Co.").build(),
                Watchlist.builder().user(user).symbol("PLD").companyName("Prologis Inc.").build(),
                Watchlist.builder().user(user).symbol("CEG").companyName("Constellation Energy").build(),
                Watchlist.builder().user(user).symbol("BLK").companyName("BlackRock Inc.").build()
        );

        watchlistRepository.saveAll(items);
    }
}
