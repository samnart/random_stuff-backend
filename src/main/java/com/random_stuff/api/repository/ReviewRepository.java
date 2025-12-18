package com.random_stuff.api.repository;
 
import com.random_stuff.api.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
 
import java.util.List;
import java.util.Optional;
 
@Repository
public interface ReviewRepository extends JpaRepository<Review, String> {
 
    List<Review> findByProductIdOrderByCreatedAtDesc(String productId);
 
    Page<Review> findByProductIdOrderByCreatedAtDesc(String productId, Pageable pageable);
 
    List<Review> findByUserIdOrderByCreatedAtDesc(String userId);
 
    Optional<Review> findByProductIdAndUserId(String productId, String userId);
 
    boolean existsByProductIdAndUserId(String productId, String userId);
 
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.product.id = :productId")
    Double findAverageRatingByProductId(@Param("productId") String productId);
 
    @Query("SELECT COUNT(r) FROM Review r WHERE r.product.id = :productId")
    Long countByProductId(@Param("productId") String productId);
}