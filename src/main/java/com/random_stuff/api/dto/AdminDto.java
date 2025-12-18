package com.random_stuff.api.dto;

import com.random_stuff.api.entity.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class AdminDto {

    // ===== PRODUCT DTO (Extended for admin) =====

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductDetail {
        private String id;
        private String name;
        private BigDecimal price;
        private BigDecimal originalPrice;
        private String description;
        private List<String> images;
        private List<String> colors;
        private List<String> sizes;
        private String categoryId;
        private String categoryName;
        private String badge;
        private boolean featured;
        private double rating;
        private int reviewCount;
        private int stock;
        private boolean active;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private List<SpecificationDto> specifications;

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class SpecificationDto {
            private String label;
            private String value;
        }

        public static ProductDetail fromEntity(Product product) {
            return ProductDetail.builder()
                .id(product.getId())
                .name(product.getName())
                .price(product.getPrice())
                .originalPrice(product.getOriginalPrice())
                .description(product.getDescription())
                .images(product.getImages())
                .colors(product.getColors())
                .sizes(product.getSizes())
                .categoryId(product.getCategory() != null ? product.getCategory().getId() : null)
                .categoryName(product.getCategory() != null ? product.getCategory().getName() : null)
                .badge(product.getBadge())
                .featured(product.isFeatured())
                .rating(product.getRating())
                .reviewCount(product.getReviewCount())
                .stock(product.getStock())
                .active(product.isActive())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .specifications(product.getSpecifications() != null ?
                    product.getSpecifications().stream()
                        .map(s -> SpecificationDto.builder()
                            .label(s.getLabel())
                            .value(s.getValue())
                            .build())
                        .toList() : List.of())
                .build();
        }
    }

    // ===== CATEGORY DTO (Extended for admin) =====

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryDetail {
        private String id;
        private String slug;
        private String name;
        private String icon;
        private String description;
        private int displayOrder;
        private boolean active;
        private int productCount;

        public static CategoryDetail fromEntity(Category category) {
            return CategoryDetail.builder()
                .id(category.getId())
                .slug(category.getSlug())
                .name(category.getName())
                .icon(category.getIcon())
                .description(category.getDescription())
                .displayOrder(category.getDisplayOrder())
                .active(category.isActive())
                .productCount(category.getProductCount())
                .build();
        }
    }

    // ===== COUPON DTO (Extended for admin) =====

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CouponDetail {
        private String id;
        private String code;
        private String type;
        private BigDecimal discount;
        private BigDecimal minOrder;
        private BigDecimal maxDiscount;
        private int usageLimit;
        private int usageCount;
        private boolean active;
        private boolean valid;
        private LocalDateTime validFrom;
        private LocalDateTime validUntil;
        private LocalDateTime createdAt;

        public static CouponDetail fromEntity(Coupon coupon) {
            return CouponDetail.builder()
                .id(coupon.getId())
                .code(coupon.getCode())
                .type(coupon.getType().name())
                .discount(coupon.getDiscount())
                .minOrder(coupon.getMinOrder())
                .maxDiscount(coupon.getMaxDiscount())
                .usageLimit(coupon.getUsageLimit())
                .usageCount(coupon.getUsageCount())
                .active(coupon.isActive())
                .valid(coupon.isValid())
                .validFrom(coupon.getValidFrom())
                .validUntil(coupon.getValidUntil())
                .createdAt(coupon.getCreatedAt())
                .build();
        }
    }

    // ===== USER DTO (Extended for admin) =====

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserDetail {
        private String id;
        private String name;
        private String email;
        private String avatar;
        private String phone;
        private String role;
        private boolean emailVerified;
        private int orderCount;
        private int reviewCount;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public static UserDetail fromEntity(User user) {
            return UserDetail.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .avatar(user.getAvatar())
                .phone(user.getPhone())
                .role(user.getRole().name())
                .emailVerified(user.isEmailVerified())
                .orderCount(user.getOrders() != null ? user.getOrders().size() : 0)
                .reviewCount(user.getReviews() != null ? user.getReviews().size() : 0)
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
        }
    }

    // ===== ORDER DTO (Extended for admin) =====

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderDetail {
        private String id;
        private UserSummary user;
        private String status;
        private List<OrderItemDto> items;
        private BigDecimal subtotal;
        private BigDecimal shippingCost;
        private BigDecimal discount;
        private BigDecimal total;
        private String couponCode;
        private ShippingAddressDto shippingAddress;
        private String paymentMethod;
        private boolean paid;
        private LocalDateTime paidAt;
        private String notes;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class UserSummary {
            private String id;
            private String name;
            private String email;
        }

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class OrderItemDto {
            private String productId;
            private String productName;
            private String productImage;
            private int quantity;
            private BigDecimal price;
            private BigDecimal subtotal;
            private String color;
            private String size;
        }

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class ShippingAddressDto {
            private String fullName;
            private String email;
            private String phone;
            private String address;
            private String city;
            private String region;
            private String notes;
        }

        public static OrderDetail fromEntity(Order order) {
            return OrderDetail.builder()
                .id(order.getId())
                .user(UserSummary.builder()
                    .id(order.getUser().getId())
                    .name(order.getUser().getName())
                    .email(order.getUser().getEmail())
                    .build())
                .status(order.getStatus().name())
                .items(order.getItems().stream()
                    .map(item -> OrderItemDto.builder()
                        .productId(item.getProduct() != null ? item.getProduct().getId() : null)
                        .productName(item.getProductName())
                        .productImage(item.getProductImage())
                        .quantity(item.getQuantity())
                        .price(item.getPrice())
                        .subtotal(item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                        .color(item.getColor())
                        .size(item.getSize())
                        .build())
                    .toList())
                .subtotal(order.getSubtotal())
                .shippingCost(order.getShippingCost())
                .discount(order.getDiscount())
                .total(order.getTotal())
                .couponCode(order.getCouponCode())
                .shippingAddress(order.getShippingAddress() != null ? ShippingAddressDto.builder()
                    .fullName(order.getShippingAddress().getFullName())
                    .email(order.getShippingAddress().getEmail())
                    .phone(order.getShippingAddress().getPhone())
                    .address(order.getShippingAddress().getAddress())
                    .city(order.getShippingAddress().getCity())
                    .region(order.getShippingAddress().getRegion())
                    .notes(order.getShippingAddress().getNotes())
                    .build() : null)
                .paymentMethod(order.getPaymentMethod() != null ? order.getPaymentMethod().name() : null)
                .paid(order.isPaid())
                .paidAt(order.getPaidAt())
                .notes(order.getNotes())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
        }
    }

    // ===== DASHBOARD STATS =====

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DashboardStats {
        private long totalUsers;
        private long totalProducts;
        private long totalOrders;
        private long totalCategories;
        private long activeCoupons;
        private BigDecimal totalRevenue;
        private long pendingOrders;
        private long lowStockProducts;
        private List<RecentOrder> recentOrders;
        private List<TopProduct> topProducts;

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class RecentOrder {
            private String id;
            private String customerName;
            private BigDecimal total;
            private String status;
            private LocalDateTime createdAt;
        }

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class TopProduct {
            private String id;
            private String name;
            private String image;
            private int orderCount;
            private BigDecimal revenue;
        }
    }
}
