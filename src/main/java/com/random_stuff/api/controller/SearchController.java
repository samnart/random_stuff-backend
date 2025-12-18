package com.random_stuff.api.controller;

import com.random_stuff.api.dto.ApiResponse;
import com.random_stuff.api.dto.ProductDto;
import com.random_stuff.api.service.SearchService;
import com.random_stuff.api.service.SearchService.SearchCriteria;
import com.random_stuff.api.service.SearchService.SearchFilters;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ProductDto>>> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) List<String> categories,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Double minRating,
            @RequestParam(required = false) List<String> colors,
            @RequestParam(required = false) List<String> sizes,
            @RequestParam(required = false) List<String> badges,
            @RequestParam(required = false) Boolean inStock,
            @RequestParam(required = false) Boolean featured,
            @RequestParam(required = false) Boolean onSale,
            @RequestParam(defaultValue = "relevance") String sortBy,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        SearchCriteria criteria = SearchCriteria.builder()
            .query(q)
            .categories(categories)
            .minPrice(minPrice)
            .maxPrice(maxPrice)
            .minRating(minRating)
            .colors(colors)
            .sizes(sizes)
            .badges(badges)
            .inStock(inStock)
            .featured(featured)
            .onSale(onSale)
            .sortBy(sortBy)
            .build();

        Pageable pageable = PageRequest.of(page, size);
        Page<ProductDto> results = searchService.advancedSearch(criteria, pageable);

        return ResponseEntity.ok(ApiResponse.success(results));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Page<ProductDto>>> searchPost(
            @RequestBody SearchCriteria criteria,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<ProductDto> results = searchService.advancedSearch(criteria, pageable);

        return ResponseEntity.ok(ApiResponse.success(results));
    }

    @GetMapping("/filters")
    public ResponseEntity<ApiResponse<SearchFilters>> getFilters() {
        SearchFilters filters = searchService.getAvailableFilters();
        return ResponseEntity.ok(ApiResponse.success(filters));
    }

    @GetMapping("/suggestions")
    public ResponseEntity<ApiResponse<List<String>>> getSuggestions(
            @RequestParam String q) {
        List<String> suggestions = searchService.getSuggestions(q);
        return ResponseEntity.ok(ApiResponse.success(suggestions));
    }
}
