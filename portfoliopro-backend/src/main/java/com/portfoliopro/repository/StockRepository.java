package com.portfoliopro.repository;

import com.portfoliopro.entity.Stock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {

    Optional<Stock> findBySymbolIgnoreCase(String symbol);

    List<Stock> findBySectorIgnoreCase(String sector);

    List<Stock> findAllByOrderByMarketCapDesc();

    @Query("SELECT s FROM Stock s WHERE LOWER(s.symbol) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(s.companyName) LIKE LOWER(CONCAT('%', :keyword, '%')) ORDER BY s.marketCap DESC")
    List<Stock> searchStocks(@Param("keyword") String keyword);

    @Query("SELECT DISTINCT s.sector FROM Stock s WHERE s.sector IS NOT NULL ORDER BY s.sector")
    List<String> findAllSectors();
}
