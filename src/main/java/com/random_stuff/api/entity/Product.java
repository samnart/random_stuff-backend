package com.random_stuff.api.entity;
 
import jakarta.persistence.*;
import lombok.*;
 
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
 
@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {
 
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
 
    @Column(nullable = false)
    private String name;
 
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;
 
    @Column(columnDefinition = "TEXT")
    private String description;
 
    @ElementCollection
    @CollectionTable(name = "product_images", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "image_url")
    @Builder.Default
    private List<String> images = new ArrayList<>();
 
    @ElementCollection
    @CollectionTable(name = "product_colors", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "color")
    @Builder.Default
    private List<String> colors = new ArrayList<>();
 
    @ElementCollection
    @CollectionTable(name = "product_sizes", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "size")
    @Builder.Default
    private List<String> sizes = new ArrayList<>();
 
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;
 
    private String badge;
 
    @Builder.Default
    private double rating = 0.0;
 
    @Builder.Default
    private int reviewCount = 0;
 
    @Builder.Default
    private int stock = 0;
 
    @Builder.Default
    private boolean active = true;
 
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
 
    private LocalDateTime updatedAt;
 
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ProductSpecification> specifications = new ArrayList<>();
 
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Review> reviews;
 
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
 
    public void addSpecification(ProductSpecification spec) {
        specifications.add(spec);
        spec.setProduct(this);
    }
 
    public void removeSpecification(ProductSpecification spec) {
        specifications.remove(spec);
        spec.setProduct(null);
    }
 
    public void updateRating() {
        if (reviews != null && !reviews.isEmpty()) {
            this.rating = reviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);
            this.reviewCount = reviews.size();
        }
    }
}