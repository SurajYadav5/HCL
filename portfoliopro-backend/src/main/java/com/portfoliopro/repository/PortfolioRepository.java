package com.portfoliopro.repository;

import com.portfoliopro.entity.Portfolio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {

    List<Portfolio> findByUserIdOrderByCurrentValueDesc(Long userId);

    Optional<Portfolio> findByUserIdAndSymbol(Long userId, String symbol);

    boolean existsByUserIdAndSymbol(Long userId, String symbol);

    @Query("SELECT SUM(p.totalInvested) FROM Portfolio p WHERE p.user.id = :userId")
    BigDecimal sumTotalInvestedByUserId(@Param("userId") Long userId);

    @Query("SELECT SUM(p.currentValue) FROM Portfolio p WHERE p.user.id = :userId")
    BigDecimal sumCurrentValueByUserId(@Param("userId") Long userId);

    @Query("SELECT SUM(p.unrealizedPnl) FROM Portfolio p WHERE p.user.id = :userId")
    BigDecimal sumUnrealizedPnlByUserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(DISTINCT p.symbol) FROM Portfolio p WHERE p.user.id = :userId")
    Long countDistinctSymbolsByUserId(@Param("userId") Long userId);
}
