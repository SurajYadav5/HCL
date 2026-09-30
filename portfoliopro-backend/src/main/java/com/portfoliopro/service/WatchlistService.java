package com.portfoliopro.service;

import com.portfoliopro.dto.response.StockQuoteResponse;
import com.portfoliopro.entity.User;
import com.portfoliopro.entity.Watchlist;
import com.portfoliopro.exception.BadRequestException;
import com.portfoliopro.exception.ResourceNotFoundException;
import com.portfoliopro.repository.UserRepository;
import com.portfoliopro.repository.WatchlistRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class WatchlistService {

    private final WatchlistRepository watchlistRepository;
    private final UserRepository userRepository;
    private final MarketDataService marketDataService;

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getWatchlist(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        return watchlistRepository.findByUserIdOrderByAddedAtDesc(user.getId()).stream()
                .map(w -> {
                    StockQuoteResponse quote = marketDataService.getQuote(w.getSymbol());
                    return Map.<String, Object>of(
                            "id", w.getId(),
                            "symbol", w.getSymbol(),
                            "companyName", w.getCompanyName() != null ? w.getCompanyName() : quote.getCompanyName(),
                            "price", quote.getPrice(),
                            "change", quote.getChange(),
                            "changePercent", quote.getChangePercent(),
                            "volume", quote.getVolume(),
                            "addedAt", w.getAddedAt().toString()
                    );
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public Watchlist addToWatchlist(String username, String symbol, String companyName) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        if (watchlistRepository.existsByUserIdAndSymbol(user.getId(), symbol.toUpperCase())) {
            throw new BadRequestException(symbol + " is already in your watchlist");
        }

        // Fetch company name if not provided
        String resolvedName = companyName;
        if (resolvedName == null || resolvedName.isBlank()) {
            resolvedName = marketDataService.getQuote(symbol.toUpperCase()).getCompanyName();
        }

        Watchlist entry = Watchlist.builder()
                .user(user)
                .symbol(symbol.toUpperCase())
                .companyName(resolvedName)
                .build();

        return watchlistRepository.save(entry);
    }

    @Transactional
    public void removeFromWatchlist(String username, String symbol) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        if (!watchlistRepository.existsByUserIdAndSymbol(user.getId(), symbol.toUpperCase())) {
            throw new ResourceNotFoundException("Watchlist item", "symbol", symbol);
        }

        watchlistRepository.deleteByUserIdAndSymbol(user.getId(), symbol.toUpperCase());
    }
}
