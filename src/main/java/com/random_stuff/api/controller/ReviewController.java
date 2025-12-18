package com.random_stuff.api.controller;
 
import com.random_stuff.api.dto.ApiResponse;
import com.random_stuff.api.dto.CreateReviewRequest;
import com.random_stuff.api.dto.ReviewDto;
import com.random_stuff.api.entity.User;
import com.random_stuff.api.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
 
import java.util.List;
 
@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {
 
    private final ReviewService reviewService;
 
    @GetMapping("/product/{productId}")
    public ResponseEntity<ApiResponse<List<ReviewDto>>> getProductReviews(@PathVariable String productId) {
        List<ReviewDto> reviews = reviewService.getProductReviews(productId);
        return ResponseEntity.ok(ApiResponse.success(reviews));
    }
 
    @PostMapping
    public ResponseEntity<ApiResponse<ReviewDto>> createReview(
        @AuthenticationPrincipal User user,
        @Valid @RequestBody CreateReviewRequest request
    ) {
        ReviewDto review = reviewService.createReview(user.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Review submitted successfully", review));
    }
 
    @PostMapping("/{id}/helpful")
    public ResponseEntity<ApiResponse<Void>> markHelpful(@PathVariable String id) {
        reviewService.markHelpful(id);
        return ResponseEntity.ok(ApiResponse.success("Marked as helpful", null));
    }
 
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteReview(
        @AuthenticationPrincipal User user,
        @PathVariable String id
    ) {
        reviewService.deleteReview(id, user.getId());
        return ResponseEntity.ok(ApiResponse.success("Review deleted", null));
    }
}