package com.random_stuff.api.dto;
 
import com.random_stuff.api.entity.Order;
import com.random_stuff.api.entity.OrderItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
 
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
 
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDto {
    private String id;
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
    private LocalDateTime createdAt;
 
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
        private String color;
        private String size;
 
        public static OrderItemDto fromEntity(OrderItem item) {
            return OrderItemDto.builder()
                .productId(item.getProduct() != null ? item.getProduct().getId() : null)
                .productName(item.getProductName())
                .productImage(item.getProductImage())
                .quantity(item.getQuantity())
                .price(item.getPrice())
                .color(item.getColor())
                .size(item.getSize())
                .build();
        }
    }
 
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ShippingAddressDto {
        private String name;
        private String address;
        private String city;
        private String region;
    }
 
    public static OrderDto fromEntity(Order order) {
        return OrderDto.builder()
            .id(order.getId())
            .status(order.getStatus().name().toLowerCase())
            .items(order.getItems().stream().map(OrderItemDto::fromEntity).toList())
            .subtotal(order.getSubtotal())
            .shippingCost(order.getShippingCost())
            .discount(order.getDiscount())
            .total(order.getTotal())
            .couponCode(order.getCouponCode())
            .shippingAddress(order.getShippingAddress() != null ? ShippingAddressDto.builder()
                .name(order.getShippingAddress().getFullName())
                .address(order.getShippingAddress().getAddress())
                .city(order.getShippingAddress().getCity())
                .region(order.getShippingAddress().getRegion())
                .build() : null)
            .paymentMethod(order.getPaymentMethod() != null ? order.getPaymentMethod().name() : null)
            .paid(order.isPaid())
            .createdAt(order.getCreatedAt())
            .build();
    }
}