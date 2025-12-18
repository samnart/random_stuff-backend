package com.random_stuff.api.controller;

import com.random_stuff.api.dto.ApiResponse;
import com.random_stuff.api.dto.ProductDto;
import com.random_stuff.api.entity.User;
import com.random_stuff.api.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationService recommendationService;

    @GetMapping("/personalized")
    public ResponseEntity<ApiResponse<List<ProductDto>>> getPersonalized(
            @AuthenticationPrincipal User user) {
        List<ProductDto> recommendations = user != null
            ? recommendationService.getPersonalizedRecommendations(user.getId())
            : recommendationService.getPopularProducts();
        return ResponseEntity.ok(ApiResponse.success(recommendations));
    }

    @GetMapping("/frequently-bought-together/{productId}")
    public ResponseEntity<ApiResponse<List<ProductDto>>> getFrequentlyBoughtTogether(
            @PathVariable String productId) {
        List<ProductDto> products = recommendationService.getFrequentlyBoughtTogether(productId);
        return ResponseEntity.ok(ApiResponse.success(products));
    }

    @GetMapping("/similar/{productId}")
    public ResponseEntity<ApiResponse<List<ProductDto>>> getSimilar(
            @PathVariable String productId) {
        List<ProductDto> products = recommendationService.getSimilarProducts(productId);
        return ResponseEntity.ok(ApiResponse.success(products));
    }

    @GetMapping("/popular")
    public ResponseEntity<ApiResponse<List<ProductDto>>> getPopular() {
        List<ProductDto> products = recommendationService.getPopularProducts();
        return ResponseEntity.ok(ApiResponse.success(products));
    }

    @GetMapping("/trending")
    public ResponseEntity<ApiResponse<List<ProductDto>>> getTrending() {
        List<ProductDto> products = recommendationService.getTrendingProducts();
        return ResponseEntity.ok(ApiResponse.success(products));
    }

    @GetMapping("/recently-viewed")
    public ResponseEntity<ApiResponse<List<ProductDto>>> getRecentlyViewed(
            @AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.ok(ApiResponse.success(List.of()));
        }
        List<ProductDto> products = recommendationService.getRecentlyViewed(user.getId());
        return ResponseEntity.ok(ApiResponse.success(products));
    }
}
