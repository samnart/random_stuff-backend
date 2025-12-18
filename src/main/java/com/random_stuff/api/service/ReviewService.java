package com.random_stuff.api.service;

import com.random_stuff.api.dto.CreateReviewRequest;
import com.random_stuff.api.dto.ReviewDto;
import com.random_stuff.api.entity.Product;
import com.random_stuff.api.entity.Review;
import com.random_stuff.api.entity.User;
import com.random_stuff.api.repository.ProductRepository;
import com.random_stuff.api.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
 
import java.util.List;
 
@Service
@RequiredArgsConstructor
public class ReviewService {
 
    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserService userService;
 
    @Transactional(readOnly = true)
    public List<ReviewDto> getProductReviews(String productId) {
        return reviewRepository.findByProductIdOrderByCreatedAtDesc(productId).stream()
            .map(ReviewDto::fromEntity)
            .toList();
    }
 
    @Transactional(readOnly = true)
    public Page<ReviewDto> getProductReviews(String productId, Pageable pageable) {
        return reviewRepository.findByProductIdOrderByCreatedAtDesc(productId, pageable)
            .map(ReviewDto::fromEntity);
    }
 
    @Transactional
    public ReviewDto createReview(String userId, CreateReviewRequest request) {
        // Check if user already reviewed this product
        if (reviewRepository.existsByProductIdAndUserId(request.getProductId(), userId)) {
            throw new RuntimeException("You have already reviewed this product");
        }
 
        User user = userService.findById(userId);
        Product product = productRepository.findById(request.getProductId())
            .orElseThrow(() -> new RuntimeException("Product not found"));
 
        Review review = Review.builder()
            .user(user)
            .product(product)
            .rating(request.getRating())
            .title(request.getTitle())
            .comment(request.getComment())
            .build();
 
        review = reviewRepository.save(review);
 
        // Update product rating
        updateProductRating(request.getProductId());
 
        return ReviewDto.fromEntity(review);
    }
 
    @Transactional
    public void markHelpful(String reviewId) {
        Review review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new RuntimeException("Review not found"));
 
        review.incrementHelpful();
        reviewRepository.save(review);
    }
 
    @Transactional
    public void deleteReview(String reviewId, String userId) {
        Review review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new RuntimeException("Review not found"));
 
        if (!review.getUser().getId().equals(userId)) {
            throw new RuntimeException("Access denied");
        }
 
        String productId = review.getProduct().getId();
        reviewRepository.delete(review);
 
        // Update product rating
        updateProductRating(productId);
    }
 
    private void updateProductRating(String productId) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new RuntimeException("Product not found"));
 
        Double avgRating = reviewRepository.findAverageRatingByProductId(productId);
        Long count = reviewRepository.countByProductId(productId);
 
        product.setRating(avgRating != null ? avgRating : 0.0);
        product.setReviewCount(count != null ? count.intValue() : 0);
 
        productRepository.save(product);
    }
}