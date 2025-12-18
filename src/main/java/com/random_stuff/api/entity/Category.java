package com.random_stuff.api.entity;

import jakarta.persistence.*;
import lombok.*;
 
import java.util.List;

@Entity
@Table(name = "categories")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {

 

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, unique = true)
    private String slug;

    @Column(nullable = false)
    private String name;

    private String icon;

    private String description;

    @Builder.Default
    private int displayOrder = 0;

    @Builder.Default
    private boolean active = true;

    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
    private List<Product> products;

    // Computed field - not persisted
    @Transient
    public int getProductCount() {
        return products != null ? products.size() : 0;
    }
}
