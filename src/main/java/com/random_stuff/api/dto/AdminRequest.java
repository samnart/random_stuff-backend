package com.random_stuff.api.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class AdminRequest {

    // ===== PRODUCT DTOs =====

    @Data
    public static class CreateProduct {
        @NotBlank(message = "Product name is required")
        @Size(min = 2, max = 200, message = "Name must be between 2 and 200 characters")
        private String name;

        @NotNull(message = "Price is required")
        @DecimalMin(value = "0.01", message = "Price must be greater than 0")
        private BigDecimal price;

        private BigDecimal originalPrice;

        private String description;

        private List<String> images;
        private List<String> colors;
        private List<String> sizes;

        private String categoryId;
        private String badge;
        private boolean featured = false;

        @Min(value = 0, message = "Stock cannot be negative")
        private int stock = 0;

        private List<ProductSpecDto> specifications;
    }

    @Data
    public static class UpdateProduct {
        @Size(min = 2, max = 200, message = "Name must be between 2 and 200 characters")
        private String name;

        @DecimalMin(value = "0.01", message = "Price must be greater than 0")
        private BigDecimal price;

        private BigDecimal originalPrice;

        private String description;

        private List<String> images;
        private List<String> colors;
        private List<String> sizes;

        private String categoryId;
        private String badge;
        private Boolean featured;
        private Boolean active;

        @Min(value = 0, message = "Stock cannot be negative")
        private Integer stock;

        private List<ProductSpecDto> specifications;
    }

    @Data
    public static class ProductSpecDto {
        @NotBlank(message = "Label is required")
        private String label;
        @NotBlank(message = "Value is required")
        private String value;
    }

    // ===== CATEGORY DTOs =====

    @Data
    public static class CreateCategory {
        @NotBlank(message = "Category name is required")
        @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
        private String name;

        @NotBlank(message = "Slug is required")
        @Pattern(regexp = "^[a-z0-9-]+$", message = "Slug must contain only lowercase letters, numbers, and hyphens")
        private String slug;

        private String icon;
        private String description;
        private int displayOrder = 0;
        private boolean active = true;
    }

    @Data
    public static class UpdateCategory {
        @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
        private String name;

        @Pattern(regexp = "^[a-z0-9-]+$", message = "Slug must contain only lowercase letters, numbers, and hyphens")
        private String slug;

        private String icon;
        private String description;
        private Integer displayOrder;
        private Boolean active;
    }

    // ===== COUPON DTOs =====

    @Data
    public static class CreateCoupon {
        @NotBlank(message = "Coupon code is required")
        @Size(min = 3, max = 50, message = "Code must be between 3 and 50 characters")
        private String code;

        @NotBlank(message = "Discount type is required")
        @Pattern(regexp = "^(PERCENT|FIXED)$", message = "Type must be PERCENT or FIXED")
        private String type;

        @NotNull(message = "Discount value is required")
        @DecimalMin(value = "0.01", message = "Discount must be greater than 0")
        private BigDecimal discount;

        @DecimalMin(value = "0", message = "Minimum order cannot be negative")
        private BigDecimal minOrder;

        @DecimalMin(value = "0", message = "Maximum discount cannot be negative")
        private BigDecimal maxDiscount;

        @Min(value = 0, message = "Usage limit cannot be negative")
        private int usageLimit = 0;

        private LocalDateTime validFrom;
        private LocalDateTime validUntil;
        private boolean active = true;
    }

    @Data
    public static class UpdateCoupon {
        @Size(min = 3, max = 50, message = "Code must be between 3 and 50 characters")
        private String code;

        @Pattern(regexp = "^(PERCENT|FIXED)$", message = "Type must be PERCENT or FIXED")
        private String type;

        @DecimalMin(value = "0.01", message = "Discount must be greater than 0")
        private BigDecimal discount;

        @DecimalMin(value = "0", message = "Minimum order cannot be negative")
        private BigDecimal minOrder;

        @DecimalMin(value = "0", message = "Maximum discount cannot be negative")
        private BigDecimal maxDiscount;

        @Min(value = 0, message = "Usage limit cannot be negative")
        private Integer usageLimit;

        private LocalDateTime validFrom;
        private LocalDateTime validUntil;
        private Boolean active;
    }

    // ===== USER DTOs =====

    @Data
    public static class UpdateUser {
        @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
        private String name;

        @Email(message = "Invalid email format")
        private String email;

        private String phone;
        private String avatar;

        @Pattern(regexp = "^(USER|ADMIN)$", message = "Role must be USER or ADMIN")
        private String role;

        private Boolean emailVerified;
    }

    // ===== ORDER DTOs =====

    @Data
    public static class UpdateOrderStatus {
        @NotBlank(message = "Status is required")
        @Pattern(regexp = "^(PROCESSING|CONFIRMED|SHIPPED|DELIVERED|CANCELLED|REFUNDED)$",
                 message = "Invalid order status")
        private String status;

        private String notes;
    }

    @Data
    public static class UpdateOrderPayment {
        private boolean paid;
        private String notes;
    }
}
