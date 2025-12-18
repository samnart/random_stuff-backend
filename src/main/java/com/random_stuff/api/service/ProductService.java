package com.random_stuff.api.service;
 
import com.random_stuff.api.dto.ProductDto;
import com.random_stuff.api.entity.Product;
import com.random_stuff.api.exception.ResourceNotFoundException;
import com.random_stuff.api.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
 
import java.math.BigDecimal;
import java.util.List;
 
@Service
@RequiredArgsConstructor
public class ProductService {
 
    private final ProductRepository productRepository;
 
    @Transactional(readOnly = true)
    public List<ProductDto> getAllProducts() {
        return productRepository.findByActiveTrue().stream()
            .map(ProductDto::fromEntity)
            .toList();
    }
 
    @Transactional(readOnly = true)
    public Page<ProductDto> getProducts(Pageable pageable) {
        return productRepository.findByActiveTrue(pageable)
            .map(ProductDto::fromEntity);
    }
 
    @Transactional(readOnly = true)
    public ProductDto getProduct(String id) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
        return ProductDto.fromEntity(product);
    }
 
    @Transactional(readOnly = true)
    public List<ProductDto> getProductsByCategory(String categorySlug) {
        return productRepository.findByCategorySlugAndActiveTrue(categorySlug).stream()
            .map(ProductDto::fromEntity)
            .toList();
    }
 
    @Transactional(readOnly = true)
    public List<ProductDto> searchProducts(String query) {
        return productRepository.searchProducts(query).stream()
            .map(ProductDto::fromEntity)
            .toList();
    }
 
    @Transactional(readOnly = true)
    public Page<ProductDto> searchProducts(String query, Pageable pageable) {
        return productRepository.searchProducts(query, pageable)
            .map(ProductDto::fromEntity);
    }
 
    @Transactional(readOnly = true)
    public List<ProductDto> getProductsByBadge(String badge) {
        return productRepository.findByBadgeAndActiveTrue(badge).stream()
            .map(ProductDto::fromEntity)
            .toList();
    }
 
    @Transactional(readOnly = true)
    public List<ProductDto> getNewArrivals() {
        return productRepository.findTop10ByActiveTrueOrderByCreatedAtDesc().stream()
            .map(ProductDto::fromEntity)
            .toList();
    }
 
    @Transactional(readOnly = true)
    public List<ProductDto> getTopRated() {
        return productRepository.findTop10ByActiveTrueOrderByRatingDesc().stream()
            .map(ProductDto::fromEntity)
            .toList();
    }
 
    @Transactional(readOnly = true)
    public List<ProductDto> getSimilarProducts(String productId) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));
 
        if (product.getCategory() == null) {
            return List.of();
        }
 
        return productRepository.findSimilarProducts(
            product.getCategory().getSlug(),
            productId,
            PageRequest.of(0, 4)
        ).stream().map(ProductDto::fromEntity).toList();
    }
 
    @Transactional(readOnly = true)
    public BigDecimal getMaxPrice() {
        return productRepository.findMaxPrice();
    }
 
    public Product findById(String id) {
        return productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
    }
}