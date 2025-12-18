package com.random_stuff.api.repository;

import com.random_stuff.api.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
 
import java.math.BigDecimal;
import java.util.List;
 
@Repository
public interface ProductRepository extends JpaRepository<Product, String> {
 
    List<Product> findByActiveTrue();
 
    Page<Product> findByActiveTrue(Pageable pageable);
 
    List<Product> findByCategorySlugAndActiveTrue(String categorySlug);
 
    Page<Product> findByCategorySlugAndActiveTrue(String categorySlug, Pageable pageable);
 
    @Query("SELECT p FROM Product p WHERE p.active = true AND " +
           "(LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<Product> searchProducts(@Param("query") String query);
 
    @Query("SELECT p FROM Product p WHERE p.active = true AND " +
           "(LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<Product> searchProducts(@Param("query") String query, Pageable pageable);
 
    List<Product> findByBadgeAndActiveTrue(String badge);
 
    @Query("SELECT p FROM Product p WHERE p.active = true AND p.price BETWEEN :minPrice AND :maxPrice")
    List<Product> findByPriceRange(@Param("minPrice") BigDecimal minPrice, @Param("maxPrice") BigDecimal maxPrice);
 
    List<Product> findTop10ByActiveTrueOrderByCreatedAtDesc();
 
    List<Product> findTop10ByActiveTrueOrderByRatingDesc();
 
    @Query("SELECT p FROM Product p WHERE p.active = true AND p.category.slug = :categorySlug AND p.id != :productId")
    List<Product> findSimilarProducts(@Param("categorySlug") String categorySlug, @Param("productId") String productId, Pageable pageable);
 
    @Query("SELECT MAX(p.price) FROM Product p WHERE p.active = true")
    BigDecimal findMaxPrice();
}