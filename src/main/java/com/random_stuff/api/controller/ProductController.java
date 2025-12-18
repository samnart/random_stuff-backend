package com.random_stuff.api.controller;
 
import com.random_stuff.api.dto.ApiResponse;
import com.random_stuff.api.dto.ProductDto;
import com.random_stuff.api.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
 
import java.math.BigDecimal;
import java.util.List;
 
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {
 
    private final ProductService productService;
 
    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductDto>>> getAllProducts(
        @RequestParam(required = false) String category,
        @RequestParam(required = false) String search,
        @RequestParam(required = false) String badge,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(defaultValue = "createdAt") String sortBy,
        @RequestParam(defaultValue = "desc") String sortDir
    ) {
        List<ProductDto> products;
 
        if (search != null && !search.isBlank()) {
            products = productService.searchProducts(search);
        } else if (category != null && !category.isBlank()) {
            products = productService.getProductsByCategory(category);
        } else if (badge != null && !badge.isBlank()) {
            products = productService.getProductsByBadge(badge);
        } else {
            products = productService.getAllProducts();
        }
 
        return ResponseEntity.ok(ApiResponse.success(products));
    }
 
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductDto>> getProduct(@PathVariable String id) {
        ProductDto product = productService.getProduct(id);
        return ResponseEntity.ok(ApiResponse.success(product));
    }
 
    @GetMapping("/{id}/similar")
    public ResponseEntity<ApiResponse<List<ProductDto>>> getSimilarProducts(@PathVariable String id) {
        List<ProductDto> products = productService.getSimilarProducts(id);
        return ResponseEntity.ok(ApiResponse.success(products));
    }
 
    @GetMapping("/new-arrivals")
    public ResponseEntity<ApiResponse<List<ProductDto>>> getNewArrivals() {
        List<ProductDto> products = productService.getNewArrivals();
        return ResponseEntity.ok(ApiResponse.success(products));
    }
 
    @GetMapping("/top-rated")
    public ResponseEntity<ApiResponse<List<ProductDto>>> getTopRated() {
        List<ProductDto> products = productService.getTopRated();
        return ResponseEntity.ok(ApiResponse.success(products));
    }
 
    @GetMapping("/max-price")
    public ResponseEntity<ApiResponse<BigDecimal>> getMaxPrice() {
        BigDecimal maxPrice = productService.getMaxPrice();
        return ResponseEntity.ok(ApiResponse.success(maxPrice));
    }
}