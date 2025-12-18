package com.random_stuff.api.dto;
 
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
 
import java.util.List;
 
@Data
public class CreateOrderRequest {
 
    @NotEmpty(message = "Order must have at least one item")
    @Valid
    private List<CartItemDto> items;
 
    @NotNull(message = "Shipping address is required")
    @Valid
    private ShippingDto shipping;
 
    @NotNull(message = "Payment method is required")
    private String paymentMethod;
 
    private String couponCode;
 
    @Data
    public static class CartItemDto {
        @NotNull(message = "Product ID is required")
        private String productId;
 
        @NotNull(message = "Quantity is required")
        private int quantity;
 
        private String color;
        private String size;
    }
 
    @Data
    public static class ShippingDto {
        @NotNull(message = "Full name is required")
        private String fullName;
 
        @NotNull(message = "Email is required")
        private String email;
 
        @NotNull(message = "Phone is required")
        private String phone;
 
        @NotNull(message = "Address is required")
        private String address;
 
        @NotNull(message = "City is required")
        private String city;
 
        @NotNull(message = "Region is required")
        private String region;
 
        private String notes;
    }
}