package com.random_stuff.api.dto;
 
import com.random_stuff.api.entity.Product;
import com.random_stuff.api.entity.ProductSpecification;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
 
import java.math.BigDecimal;
import java.util.List;
 
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDto {
    private String id;
    private String name;
    private BigDecimal price;
    private String description;
    private List<String> images;
    private List<String> colors;
    private List<String> sizes;
    private String category;
    private String badge;
    private double rating;
    private int reviewCount;
    private int stock;
    private List<SpecificationDto> specifications;
 
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SpecificationDto {
        private String label;
        private String value;
 
        public static SpecificationDto fromEntity(ProductSpecification spec) {
            return SpecificationDto.builder()
                .label(spec.getLabel())
                .value(spec.getValue())
                .build();
        }
    }
 
    public static ProductDto fromEntity(Product product) {
        return ProductDto.builder()
            .id(product.getId())
            .name(product.getName())
            .price(product.getPrice())
            .description(product.getDescription())
            .images(product.getImages())
            .colors(product.getColors())
            .sizes(product.getSizes())
            .category(product.getCategory() != null ? product.getCategory().getSlug() : null)
            .badge(product.getBadge())
            .rating(product.getRating())
            .reviewCount(product.getReviewCount())
            .stock(product.getStock())
            .specifications(product.getSpecifications() != null ?
                product.getSpecifications().stream()
                    .map(SpecificationDto::fromEntity)
                    .toList() : List.of())
            .build();
    }
}