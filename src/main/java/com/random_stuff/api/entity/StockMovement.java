package com.random_stuff.api.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "stock_movements")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockMovement {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MovementType type;

    @Column(nullable = false)
    private int quantity;

    private int previousStock;

    private int newStock;

    private String reason;

    private String referenceId; // Order ID, adjustment ID, etc.

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User performedBy;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum MovementType {
        SALE,           // Stock reduced due to sale
        RESTOCK,        // Stock added from supplier
        ADJUSTMENT,     // Manual adjustment
        RETURN,         // Stock returned from customer
        DAMAGE,         // Stock lost due to damage
        TRANSFER,       // Transferred between locations
        INITIAL         // Initial stock setup
    }
}
