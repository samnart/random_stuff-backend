package com.random_stuff.api.controller;
 
import com.random_stuff.api.dto.ApiResponse;
import com.random_stuff.api.dto.CreateOrderRequest;
import com.random_stuff.api.dto.OrderDto;
import com.random_stuff.api.entity.User;
import com.random_stuff.api.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
 
import java.util.List;
 
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
 
    private final OrderService orderService;
 
    @PostMapping
    public ResponseEntity<ApiResponse<OrderDto>> createOrder(
        @AuthenticationPrincipal User user,
        @Valid @RequestBody CreateOrderRequest request
    ) {
        OrderDto order = orderService.createOrder(user.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Order placed successfully", order));
    }
 
    @GetMapping
    public ResponseEntity<ApiResponse<List<OrderDto>>> getUserOrders(@AuthenticationPrincipal User user) {
        List<OrderDto> orders = orderService.getUserOrders(user.getId());
        return ResponseEntity.ok(ApiResponse.success(orders));
    }
 
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderDto>> getOrder(
        @AuthenticationPrincipal User user,
        @PathVariable String id
    ) {
        OrderDto order = orderService.getOrder(id, user.getId());
        return ResponseEntity.ok(ApiResponse.success(order));
    }
}