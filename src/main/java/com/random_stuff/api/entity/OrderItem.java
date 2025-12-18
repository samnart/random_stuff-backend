package com.random_stuff.api.entity;
 
import jakarta.persistence.*;
import lombok.*;
 
import java.math.BigDecimal;
 
@Entity
@Table(name = "order_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {
 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
 
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Order order;
 
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
 
    // Denormalized product info (in case product is deleted/modified)
    @Column(nullable = false)
    private String productName;
 
    private String productImage;
 
    @Column(nullable = false)
    private int quantity;
 
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;
 
    private String color;
 
    private String size;
 
    public BigDecimal getSubtotal() {
        return price.multiply(BigDecimal.valueOf(quantity));
    }
}