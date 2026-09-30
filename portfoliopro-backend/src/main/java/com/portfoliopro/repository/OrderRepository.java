package com.portfoliopro.repository;

import com.portfoliopro.entity.Order;
import com.portfoliopro.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    Page<Order> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    List<Order> findByUserIdAndSymbolOrderByCreatedAtDesc(Long userId, String symbol);

    List<Order> findByUserIdAndStatus(Long userId, OrderStatus status);

    @Query("SELECT o FROM Order o WHERE o.user.id = :userId ORDER BY o.createdAt DESC")
    List<Order> findRecentByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.user.id = :userId AND o.status = 'EXECUTED'")
    Long countExecutedByUserId(@Param("userId") Long userId);

    @Query("SELECT SUM(o.totalValue) FROM Order o WHERE o.user.id = :userId AND o.status = 'EXECUTED' AND o.side = 'BUY'")
    java.math.BigDecimal sumBuyVolumeByUserId(@Param("userId") Long userId);
}
