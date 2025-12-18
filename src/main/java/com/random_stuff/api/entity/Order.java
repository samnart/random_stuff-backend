package com.random_stuff.api.entity;
 
import jakarta.persistence.*;
import lombok.*;
 
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
 
@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {
 
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
 
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User user;
 
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private OrderStatus status = OrderStatus.PROCESSING;
 
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();
 
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;
 
    @Column(precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal shippingCost = BigDecimal.ZERO;
 
    @Column(precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal discount = BigDecimal.ZERO;
 
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal total;
 
    private String couponCode;
 
    // Shipping Address (embedded)
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(
            name = "notes",
            column = @Column(name = "shipping_notes")
        )
    })
    private ShippingAddress shippingAddress;
 
    // Payment Info
    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;
 
    @Builder.Default
    private boolean paid = false;
 
    private LocalDateTime paidAt;
 
    private String notes;
 
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
 
    private LocalDateTime updatedAt;
 
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
 
    public void addItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
    }
 
    public void removeItem(OrderItem item) {
        items.remove(item);
        item.setOrder(null);
    }
 
    public void calculateTotal() {
        this.subtotal = items.stream()
            .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        this.total = subtotal.add(shippingCost).subtract(discount);
    }
 
    public enum OrderStatus {
        PROCESSING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED, REFUNDED
    }
 
    public enum PaymentMethod {
        CARD, MOBILE_MONEY, CASH_ON_DELIVERY
    }
}