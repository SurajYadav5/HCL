package com.portfoliopro.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "watchlist", indexes = {
    @Index(name = "idx_watchlist_user_symbol", columnList = "user_id, symbol", unique = true)
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Watchlist {

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

    @Column(name = "sector", length = 50)
    private String sector;

    @Column(name = "added_at", nullable = false, updatable = false)
    private LocalDateTime addedAt;

    @Column(name = "notes", length = 200)
    private String notes;

    @PrePersist
    protected void onCreate() {
        addedAt = LocalDateTime.now();
    }
}
