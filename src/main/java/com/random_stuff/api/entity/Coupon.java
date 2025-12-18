package com.random_stuff.api.entity;
 
import jakarta.persistence.*;
import lombok.*;
 
import java.math.BigDecimal;
import java.time.LocalDateTime;
 
@Entity
@Table(name = "coupons")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Coupon {
 
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
 
    @Column(nullable = false, unique = true)
    private String code;
 
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DiscountType type;
 
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal discount;
 
    @Column(precision = 10, scale = 2)
    private BigDecimal minOrder;
 
    @Column(precision = 10, scale = 2)
    private BigDecimal maxDiscount;
 
    @Builder.Default
    private int usageLimit = 0; // 0 = unlimited
 
    @Builder.Default
    private int usageCount = 0;
 
    @Builder.Default
    private boolean active = true;
 
    private LocalDateTime validFrom;
 
    private LocalDateTime validUntil;
 
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
 
    public boolean isValid() {
        if (!active) return false;
        if (usageLimit > 0 && usageCount >= usageLimit) return false;
 
        LocalDateTime now = LocalDateTime.now();
        if (validFrom != null && now.isBefore(validFrom)) return false;
        if (validUntil != null && now.isAfter(validUntil)) return false;
 
        return true;
    }
 
    public BigDecimal calculateDiscount(BigDecimal orderTotal) {
        if (!isValid()) return BigDecimal.ZERO;
        if (minOrder != null && orderTotal.compareTo(minOrder) < 0) return BigDecimal.ZERO;
 
        BigDecimal discountAmount;
        if (type == DiscountType.PERCENT) {
            discountAmount = orderTotal.multiply(discount).divide(BigDecimal.valueOf(100));
        } else {
            discountAmount = discount;
        }
 
        if (maxDiscount != null && discountAmount.compareTo(maxDiscount) > 0) {
            discountAmount = maxDiscount;
        }
 
        return discountAmount;
    }
 
    public void incrementUsage() {
        this.usageCount++;
    }
 
    public enum DiscountType {
        PERCENT, FIXED
    }
}
