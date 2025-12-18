package com.random_stuff.api.service;

import com.random_stuff.api.dto.ProductDto;
import com.random_stuff.api.entity.Product;
import com.random_stuff.api.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchService {

    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public Page<ProductDto> advancedSearch(SearchCriteria criteria, Pageable pageable) {
        // Start with all active products
        Stream<Product> productStream = productRepository.findByActiveTrue().stream();

        // Apply filters
        if (criteria.getQuery() != null && !criteria.getQuery().isBlank()) {
            String query = criteria.getQuery().toLowerCase();
            productStream = productStream.filter(p ->
                p.getName().toLowerCase().contains(query) ||
                (p.getDescription() != null && p.getDescription().toLowerCase().contains(query))
            );
        }

        if (criteria.getCategories() != null && !criteria.getCategories().isEmpty()) {
            productStream = productStream.filter(p ->
                p.getCategory() != null &&
                criteria.getCategories().contains(p.getCategory().getSlug())
            );
        }

        if (criteria.getMinPrice() != null) {
            productStream = productStream.filter(p ->
                p.getPrice().compareTo(criteria.getMinPrice()) >= 0
            );
        }

        if (criteria.getMaxPrice() != null) {
            productStream = productStream.filter(p ->
                p.getPrice().compareTo(criteria.getMaxPrice()) <= 0
            );
        }

        if (criteria.getMinRating() != null) {
            productStream = productStream.filter(p ->
                p.getRating() >= criteria.getMinRating()
            );
        }

        if (criteria.getColors() != null && !criteria.getColors().isEmpty()) {
            productStream = productStream.filter(p ->
                p.getColors() != null &&
                p.getColors().stream().anyMatch(c ->
                    criteria.getColors().stream().anyMatch(fc ->
                        c.equalsIgnoreCase(fc)))
            );
        }

        if (criteria.getSizes() != null && !criteria.getSizes().isEmpty()) {
            productStream = productStream.filter(p ->
                p.getSizes() != null &&
                p.getSizes().stream().anyMatch(s ->
                    criteria.getSizes().contains(s))
            );
        }

        if (criteria.getBadges() != null && !criteria.getBadges().isEmpty()) {
            productStream = productStream.filter(p ->
                p.getBadge() != null &&
                criteria.getBadges().contains(p.getBadge())
            );
        }

        if (Boolean.TRUE.equals(criteria.getInStock())) {
            productStream = productStream.filter(p -> p.getStock() > 0);
        }

        if (Boolean.TRUE.equals(criteria.getFeatured())) {
            productStream = productStream.filter(Product::isFeatured);
        }

        if (Boolean.TRUE.equals(criteria.getOnSale())) {
            productStream = productStream.filter(p ->
                p.getOriginalPrice() != null &&
                p.getOriginalPrice().compareTo(p.getPrice()) > 0
            );
        }

        // Collect and sort
        List<Product> filteredProducts = productStream.toList();

        // Apply sorting
        List<Product> sortedProducts = switch (criteria.getSortBy() != null ? criteria.getSortBy() : "relevance") {
            case "price_asc" -> filteredProducts.stream()
                .sorted((a, b) -> a.getPrice().compareTo(b.getPrice()))
                .toList();
            case "price_desc" -> filteredProducts.stream()
                .sorted((a, b) -> b.getPrice().compareTo(a.getPrice()))
                .toList();
            case "rating" -> filteredProducts.stream()
                .sorted((a, b) -> Double.compare(b.getRating(), a.getRating()))
                .toList();
            case "newest" -> filteredProducts.stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .toList();
            case "name" -> filteredProducts.stream()
                .sorted((a, b) -> a.getName().compareToIgnoreCase(b.getName()))
                .toList();
            case "popularity" -> filteredProducts.stream()
                .sorted((a, b) -> Integer.compare(b.getReviewCount(), a.getReviewCount()))
                .toList();
            default -> filteredProducts; // relevance - keep original order
        };

        // Apply pagination
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), sortedProducts.size());

        List<ProductDto> pageContent = sortedProducts.subList(start, end).stream()
            .map(ProductDto::fromEntity)
            .toList();

        return new PageImpl<>(pageContent, pageable, sortedProducts.size());
    }

    @Transactional(readOnly = true)
    public SearchFilters getAvailableFilters() {
        List<Product> products = productRepository.findByActiveTrue();

        // Collect unique values for filters
        List<String> categories = products.stream()
            .filter(p -> p.getCategory() != null)
            .map(p -> p.getCategory().getSlug())
            .distinct()
            .toList();

        List<String> colors = products.stream()
            .filter(p -> p.getColors() != null)
            .flatMap(p -> p.getColors().stream())
            .distinct()
            .sorted()
            .toList();

        List<String> sizes = products.stream()
            .filter(p -> p.getSizes() != null)
            .flatMap(p -> p.getSizes().stream())
            .distinct()
            .toList();

        List<String> badges = products.stream()
            .filter(p -> p.getBadge() != null)
            .map(Product::getBadge)
            .distinct()
            .toList();

        BigDecimal minPrice = products.stream()
            .map(Product::getPrice)
            .min(BigDecimal::compareTo)
            .orElse(BigDecimal.ZERO);

        BigDecimal maxPrice = products.stream()
            .map(Product::getPrice)
            .max(BigDecimal::compareTo)
            .orElse(BigDecimal.valueOf(1000));

        return SearchFilters.builder()
            .categories(categories)
            .colors(colors)
            .sizes(sizes)
            .badges(badges)
            .minPrice(minPrice)
            .maxPrice(maxPrice)
            .build();
    }

    @Transactional(readOnly = true)
    public List<String> getSuggestions(String query) {
        if (query == null || query.length() < 2) {
            return List.of();
        }

        String lowerQuery = query.toLowerCase();

        // Get matching product names
        return productRepository.findByActiveTrue().stream()
            .filter(p -> p.getName().toLowerCase().contains(lowerQuery))
            .map(Product::getName)
            .distinct()
            .limit(10)
            .toList();
    }

    // ===== DTOs =====

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class SearchCriteria {
        private String query;
        private List<String> categories;
        private BigDecimal minPrice;
        private BigDecimal maxPrice;
        private Double minRating;
        private List<String> colors;
        private List<String> sizes;
        private List<String> badges;
        private Boolean inStock;
        private Boolean featured;
        private Boolean onSale;
        private String sortBy;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class SearchFilters {
        private List<String> categories;
        private List<String> colors;
        private List<String> sizes;
        private List<String> badges;
        private BigDecimal minPrice;
        private BigDecimal maxPrice;
    }
}
