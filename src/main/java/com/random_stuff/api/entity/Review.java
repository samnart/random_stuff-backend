package com.random_stuff.api.entity;
 
import jakarta.persistence.*;
import lombok.*;
 
import java.time.LocalDateTime;
 
@Entity
@Table(name = "reviews")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review {
 
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
 
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Product product;
 
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User user;
 
    @Column(nullable = false)
    private int rating;
 
    private String title;
 
    @Column(columnDefinition = "TEXT")
    private String comment;
 
    @Builder.Default
    private int helpful = 0;
 
    @Builder.Default
    private boolean verified = false;
 
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
 
    private LocalDateTime updatedAt;
 
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
 
    public void incrementHelpful() {
        this.helpful++;
    }
}