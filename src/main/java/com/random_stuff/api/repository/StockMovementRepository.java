package com.random_stuff.api.repository;

import com.random_stuff.api.entity.StockMovement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StockMovementRepository extends JpaRepository<StockMovement, String> {

    List<StockMovement> findByProductIdOrderByCreatedAtDesc(String productId);

    Page<StockMovement> findByProductIdOrderByCreatedAtDesc(String productId, Pageable pageable);

    List<StockMovement> findByTypeOrderByCreatedAtDesc(StockMovement.MovementType type);

    @Query("SELECT sm FROM StockMovement sm WHERE sm.createdAt >= :startDate ORDER BY sm.createdAt DESC")
    List<StockMovement> findRecentMovements(@Param("startDate") LocalDateTime startDate);

    @Query("SELECT sm FROM StockMovement sm WHERE sm.product.id = :productId AND sm.createdAt >= :startDate")
    List<StockMovement> findByProductIdAndDateAfter(
        @Param("productId") String productId,
        @Param("startDate") LocalDateTime startDate
    );
}
