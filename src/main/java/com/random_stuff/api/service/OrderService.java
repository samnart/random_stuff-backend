package com.random_stuff.api.service;
 
import com.random_stuff.api.dto.CreateOrderRequest;
import com.random_stuff.api.dto.OrderDto;
import com.random_stuff.api.entity.*;
import com.random_stuff.api.exception.BadRequestException;
import com.random_stuff.api.exception.ResourceNotFoundException;
import com.random_stuff.api.exception.UnauthorizedException;
import com.random_stuff.api.repository.OrderRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
 
import java.math.BigDecimal;
import java.util.List;
 
@Service
@RequiredArgsConstructor
public class OrderService {
 
    private final OrderRepository orderRepository;
    private final ProductService productService;
    private final UserService userService;
    private final CouponService couponService;
 
    @Transactional
    public OrderDto createOrder(String userId, CreateOrderRequest request) {
        User user = userService.findById(userId);
 
        Order order = Order.builder()
            .user(user)
            .shippingAddress(ShippingAddress.builder()
                .fullName(request.getShipping().getFullName())
                .email(request.getShipping().getEmail())
                .phone(request.getShipping().getPhone())
                .address(request.getShipping().getAddress())
                .city(request.getShipping().getCity())
                .region(request.getShipping().getRegion())
                .notes(request.getShipping().getNotes())
                .build())
            .paymentMethod(Order.PaymentMethod.valueOf(request.getPaymentMethod().toUpperCase()))
            .build();
 
        // Add items
        for (CreateOrderRequest.CartItemDto itemDto : request.getItems()) {
            Product product = productService.findById(itemDto.getProductId());
 
            if (product.getStock() < itemDto.getQuantity()) {
                throw new BadRequestException("Insufficient stock for product: " + product.getName());
            }
 
            OrderItem item = OrderItem.builder()
                .product(product)
                .productName(product.getName())
                .productImage(product.getImages().isEmpty() ? null : product.getImages().get(0))
                .quantity(itemDto.getQuantity())
                .price(product.getPrice())
                .color(itemDto.getColor())
                .size(itemDto.getSize())
                .build();
 
            order.addItem(item);
 
            // Reduce stock
            product.setStock(product.getStock() - itemDto.getQuantity());
        }
 
        // Calculate totals
        order.calculateTotal();
 
        // Apply shipping (free over 200)
        if (order.getSubtotal().compareTo(BigDecimal.valueOf(200)) < 0) {
            order.setShippingCost(BigDecimal.valueOf(15));
        }
 
        // Apply coupon
        if (request.getCouponCode() != null && !request.getCouponCode().isBlank()) {
            BigDecimal discount = couponService.applyCoupon(request.getCouponCode(), order.getSubtotal());
            order.setDiscount(discount);
            order.setCouponCode(request.getCouponCode());
        }
 
        // Recalculate total with shipping and discount
        order.setTotal(order.getSubtotal().add(order.getShippingCost()).subtract(order.getDiscount()));
 
        order = orderRepository.save(order);
 
        return OrderDto.fromEntity(order);
    }
 
    @Transactional(readOnly = true)
    public List<OrderDto> getUserOrders(String userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
            .map(OrderDto::fromEntity)
            .toList();
    }
 
    @Transactional(readOnly = true)
    public OrderDto getOrder(String orderId, String userId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));
 
        if (!order.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("Access denied to this order");
        }
 
        return OrderDto.fromEntity(order);
    }
 
    @Transactional
    public OrderDto updateOrderStatus(String orderId, Order.OrderStatus status) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));
 
        order.setStatus(status);
 
        return OrderDto.fromEntity(orderRepository.save(order));
    }
}