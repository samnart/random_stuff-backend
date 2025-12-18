package com.random_stuff.api.service;

import com.random_stuff.api.dto.ProductDto;
import com.random_stuff.api.entity.Order;
import com.random_stuff.api.entity.Product;
import com.random_stuff.api.entity.User;
import com.random_stuff.api.repository.OrderRepository;
import com.random_stuff.api.repository.ProductRepository;
import com.random_stuff.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecommendationService {

    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    @Value("${recommendations.max-items:10}")
    private int maxRecommendations;

    @Value("${recommendations.min-orders-for-collaborative:5}")
    private int minOrdersForCollaborative;

    /**
     * Get personalized recommendations for a user
     * Combines collaborative filtering with content-based recommendations
     */
    @Transactional(readOnly = true)
    public List<ProductDto> getPersonalizedRecommendations(String userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return getPopularProducts();
        }

        Set<String> recommendedIds = new LinkedHashSet<>();

        // 1. Get products from wishlist categories (content-based)
        List<Product> wishlist = user.getWishlist();
        if (wishlist != null && !wishlist.isEmpty()) {
            Set<String> wishlistCategories = wishlist.stream()
                .filter(p -> p.getCategory() != null)
                .map(p -> p.getCategory().getSlug())
                .collect(Collectors.toSet());

            for (String categorySlug : wishlistCategories) {
                productRepository.findByCategorySlugAndActiveTrue(categorySlug).stream()
                    .filter(p -> !wishlist.contains(p))
                    .limit(3)
                    .forEach(p -> recommendedIds.add(p.getId()));
            }
        }

        // 2. Get "frequently bought together" (collaborative filtering)
        List<Order> userOrders = orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
        if (userOrders.size() >= minOrdersForCollaborative) {
            Set<String> purchasedProductIds = userOrders.stream()
                .flatMap(o -> o.getItems().stream())
                .filter(item -> item.getProduct() != null)
                .map(item -> item.getProduct().getId())
                .collect(Collectors.toSet());

            // Find other users who bought the same products
            List<String> similarUserIds = findSimilarUsers(purchasedProductIds, userId);

            // Get products those users bought that this user hasn't
            for (String similarUserId : similarUserIds) {
                orderRepository.findByUserIdOrderByCreatedAtDesc(similarUserId).stream()
                    .flatMap(o -> o.getItems().stream())
                    .filter(item -> item.getProduct() != null)
                    .map(item -> item.getProduct().getId())
                    .filter(id -> !purchasedProductIds.contains(id))
                    .limit(2)
                    .forEach(recommendedIds::add);
            }
        }

        // 3. Fill remaining slots with popular products
        if (recommendedIds.size() < maxRecommendations) {
            productRepository.findTop10ByActiveTrueOrderByRatingDesc().stream()
                .map(Product::getId)
                .filter(id -> !recommendedIds.contains(id))
                .limit(maxRecommendations - recommendedIds.size())
                .forEach(recommendedIds::add);
        }

        // Convert to ProductDto
        return recommendedIds.stream()
            .limit(maxRecommendations)
            .map(id -> productRepository.findById(id).orElse(null))
            .filter(Objects::nonNull)
            .map(ProductDto::fromEntity)
            .toList();
    }

    /**
     * Get products frequently bought together with a specific product
     */
    @Transactional(readOnly = true)
    public List<ProductDto> getFrequentlyBoughtTogether(String productId) {
        // Find orders containing this product
        List<Order> ordersWithProduct = orderRepository.findAll().stream()
            .filter(order -> order.getItems().stream()
                .anyMatch(item -> item.getProduct() != null &&
                         item.getProduct().getId().equals(productId)))
            .toList();

        // Count co-occurrences of other products
        Map<String, Long> coOccurrences = new HashMap<>();
        for (Order order : ordersWithProduct) {
            order.getItems().stream()
                .filter(item -> item.getProduct() != null &&
                       !item.getProduct().getId().equals(productId))
                .forEach(item -> coOccurrences.merge(
                    item.getProduct().getId(), 1L, Long::sum));
        }

        // Sort by frequency and return top products
        return coOccurrences.entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .limit(4)
            .map(entry -> productRepository.findById(entry.getKey()).orElse(null))
            .filter(Objects::nonNull)
            .filter(Product::isActive)
            .map(ProductDto::fromEntity)
            .toList();
    }

    /**
     * Get recently viewed products for a user (based on order history for now)
     * In production, this would use a separate view tracking table
     */
    @Transactional(readOnly = true)
    public List<ProductDto> getRecentlyViewed(String userId) {
        // For now, return products from recent orders
        // In production, implement view tracking
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
            .limit(5)
            .flatMap(order -> order.getItems().stream())
            .filter(item -> item.getProduct() != null)
            .map(item -> item.getProduct())
            .distinct()
            .limit(maxRecommendations)
            .map(ProductDto::fromEntity)
            .toList();
    }

    /**
     * Get popular products (bestsellers)
     */
    @Transactional(readOnly = true)
    public List<ProductDto> getPopularProducts() {
        // Count product occurrences in orders
        Map<String, Long> productOrderCounts = new HashMap<>();

        orderRepository.findAll().stream()
            .filter(order -> order.getCreatedAt().isAfter(LocalDateTime.now().minusDays(30)))
            .flatMap(order -> order.getItems().stream())
            .filter(item -> item.getProduct() != null)
            .forEach(item -> productOrderCounts.merge(
                item.getProduct().getId(),
                (long) item.getQuantity(),
                Long::sum));

        if (productOrderCounts.isEmpty()) {
            // Fallback to top-rated products
            return productRepository.findTop10ByActiveTrueOrderByRatingDesc().stream()
                .map(ProductDto::fromEntity)
                .toList();
        }

        return productOrderCounts.entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .limit(maxRecommendations)
            .map(entry -> productRepository.findById(entry.getKey()).orElse(null))
            .filter(Objects::nonNull)
            .filter(Product::isActive)
            .map(ProductDto::fromEntity)
            .toList();
    }

    /**
     * Get trending products (based on recent sales velocity)
     */
    @Transactional(readOnly = true)
    public List<ProductDto> getTrendingProducts() {
        LocalDateTime oneWeekAgo = LocalDateTime.now().minusDays(7);
        LocalDateTime twoWeeksAgo = LocalDateTime.now().minusDays(14);

        // Calculate sales in last week
        Map<String, Long> recentSales = new HashMap<>();
        orderRepository.findAll().stream()
            .filter(order -> order.getCreatedAt().isAfter(oneWeekAgo))
            .flatMap(order -> order.getItems().stream())
            .filter(item -> item.getProduct() != null)
            .forEach(item -> recentSales.merge(
                item.getProduct().getId(),
                (long) item.getQuantity(),
                Long::sum));

        // Calculate sales in previous week
        Map<String, Long> previousSales = new HashMap<>();
        orderRepository.findAll().stream()
            .filter(order -> order.getCreatedAt().isAfter(twoWeeksAgo) &&
                           order.getCreatedAt().isBefore(oneWeekAgo))
            .flatMap(order -> order.getItems().stream())
            .filter(item -> item.getProduct() != null)
            .forEach(item -> previousSales.merge(
                item.getProduct().getId(),
                (long) item.getQuantity(),
                Long::sum));

        // Calculate growth rate
        Map<String, Double> growthRates = new HashMap<>();
        for (String productId : recentSales.keySet()) {
            long recent = recentSales.getOrDefault(productId, 0L);
            long previous = previousSales.getOrDefault(productId, 1L);
            double growth = (double) recent / previous;
            growthRates.put(productId, growth);
        }

        return growthRates.entrySet().stream()
            .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
            .limit(maxRecommendations)
            .map(entry -> productRepository.findById(entry.getKey()).orElse(null))
            .filter(Objects::nonNull)
            .filter(Product::isActive)
            .map(ProductDto::fromEntity)
            .toList();
    }

    /**
     * Get similar products based on category and attributes
     */
    @Transactional(readOnly = true)
    public List<ProductDto> getSimilarProducts(String productId) {
        Product product = productRepository.findById(productId).orElse(null);
        if (product == null || product.getCategory() == null) {
            return List.of();
        }

        // Get products from same category
        List<Product> sameCategory = productRepository
            .findByCategorySlugAndActiveTrue(product.getCategory().getSlug());

        // Score by similarity (shared colors, sizes, price range)
        Map<Product, Double> similarityScores = new HashMap<>();

        for (Product other : sameCategory) {
            if (other.getId().equals(productId)) continue;

            double score = 0;

            // Same badge bonus
            if (product.getBadge() != null && product.getBadge().equals(other.getBadge())) {
                score += 2;
            }

            // Shared colors
            if (product.getColors() != null && other.getColors() != null) {
                long sharedColors = product.getColors().stream()
                    .filter(c -> other.getColors().contains(c)).count();
                score += sharedColors * 0.5;
            }

            // Similar price (within 20%)
            double priceDiff = Math.abs(product.getPrice().doubleValue() -
                                       other.getPrice().doubleValue());
            double avgPrice = (product.getPrice().doubleValue() +
                              other.getPrice().doubleValue()) / 2;
            if (priceDiff / avgPrice < 0.2) {
                score += 1;
            }

            // Rating bonus
            score += other.getRating() / 5.0;

            similarityScores.put(other, score);
        }

        return similarityScores.entrySet().stream()
            .sorted(Map.Entry.<Product, Double>comparingByValue().reversed())
            .limit(4)
            .map(entry -> ProductDto.fromEntity(entry.getKey()))
            .toList();
    }

    // ===== HELPER METHODS =====

    private List<String> findSimilarUsers(Set<String> productIds, String excludeUserId) {
        Map<String, Long> userOverlap = new HashMap<>();

        // Find users who bought the same products
        for (Order order : orderRepository.findAll()) {
            if (order.getUser().getId().equals(excludeUserId)) continue;

            long overlap = order.getItems().stream()
                .filter(item -> item.getProduct() != null &&
                       productIds.contains(item.getProduct().getId()))
                .count();

            if (overlap > 0) {
                userOverlap.merge(order.getUser().getId(), overlap, Long::sum);
            }
        }

        return userOverlap.entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .limit(5)
            .map(Map.Entry::getKey)
            .toList();
    }
}
