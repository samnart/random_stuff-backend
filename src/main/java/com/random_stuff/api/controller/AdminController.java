package com.random_stuff.api.controller;

import com.random_stuff.api.dto.AdminDto;
import com.random_stuff.api.dto.AdminRequest;
import com.random_stuff.api.dto.ApiResponse;
import com.random_stuff.api.dto.ReviewDto;
import com.random_stuff.api.entity.Order;
import com.random_stuff.api.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    // ===== DASHBOARD =====

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<AdminDto.DashboardStats>> getDashboard() {
        AdminDto.DashboardStats stats = adminService.getDashboardStats();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    // ===== PRODUCTS =====

    @GetMapping("/products")
    public ResponseEntity<ApiResponse<Page<AdminDto.ProductDetail>>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("asc")
            ? Sort.by(sortBy).ascending()
            : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<AdminDto.ProductDetail> products = adminService.getAllProducts(pageable);
        return ResponseEntity.ok(ApiResponse.success(products));
    }

    @GetMapping("/products/{id}")
    public ResponseEntity<ApiResponse<AdminDto.ProductDetail>> getProduct(@PathVariable String id) {
        AdminDto.ProductDetail product = adminService.getProduct(id);
        return ResponseEntity.ok(ApiResponse.success(product));
    }

    @PostMapping("/products")
    public ResponseEntity<ApiResponse<AdminDto.ProductDetail>> createProduct(
            @Valid @RequestBody AdminRequest.CreateProduct request) {
        AdminDto.ProductDetail product = adminService.createProduct(request);
        return ResponseEntity.ok(ApiResponse.success("Product created successfully", product));
    }

    @PutMapping("/products/{id}")
    public ResponseEntity<ApiResponse<AdminDto.ProductDetail>> updateProduct(
            @PathVariable String id,
            @Valid @RequestBody AdminRequest.UpdateProduct request) {
        AdminDto.ProductDetail product = adminService.updateProduct(id, request);
        return ResponseEntity.ok(ApiResponse.success("Product updated successfully", product));
    }

    @DeleteMapping("/products/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable String id) {
        adminService.deleteProduct(id);
        return ResponseEntity.ok(ApiResponse.success("Product deleted successfully", null));
    }

    @DeleteMapping("/products/{id}/permanent")
    public ResponseEntity<ApiResponse<Void>> hardDeleteProduct(@PathVariable String id) {
        adminService.hardDeleteProduct(id);
        return ResponseEntity.ok(ApiResponse.success("Product permanently deleted", null));
    }

    // ===== CATEGORIES =====

    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<AdminDto.CategoryDetail>>> getAllCategories() {
        List<AdminDto.CategoryDetail> categories = adminService.getAllCategories();
        return ResponseEntity.ok(ApiResponse.success(categories));
    }

    @GetMapping("/categories/{id}")
    public ResponseEntity<ApiResponse<AdminDto.CategoryDetail>> getCategory(@PathVariable String id) {
        AdminDto.CategoryDetail category = adminService.getCategory(id);
        return ResponseEntity.ok(ApiResponse.success(category));
    }

    @PostMapping("/categories")
    public ResponseEntity<ApiResponse<AdminDto.CategoryDetail>> createCategory(
            @Valid @RequestBody AdminRequest.CreateCategory request) {
        AdminDto.CategoryDetail category = adminService.createCategory(request);
        return ResponseEntity.ok(ApiResponse.success("Category created successfully", category));
    }

    @PutMapping("/categories/{id}")
    public ResponseEntity<ApiResponse<AdminDto.CategoryDetail>> updateCategory(
            @PathVariable String id,
            @Valid @RequestBody AdminRequest.UpdateCategory request) {
        AdminDto.CategoryDetail category = adminService.updateCategory(id, request);
        return ResponseEntity.ok(ApiResponse.success("Category updated successfully", category));
    }

    @DeleteMapping("/categories/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable String id) {
        adminService.deleteCategory(id);
        return ResponseEntity.ok(ApiResponse.success("Category deleted successfully", null));
    }

    // ===== COUPONS =====

    @GetMapping("/coupons")
    public ResponseEntity<ApiResponse<List<AdminDto.CouponDetail>>> getAllCoupons() {
        List<AdminDto.CouponDetail> coupons = adminService.getAllCoupons();
        return ResponseEntity.ok(ApiResponse.success(coupons));
    }

    @GetMapping("/coupons/{id}")
    public ResponseEntity<ApiResponse<AdminDto.CouponDetail>> getCoupon(@PathVariable String id) {
        AdminDto.CouponDetail coupon = adminService.getCoupon(id);
        return ResponseEntity.ok(ApiResponse.success(coupon));
    }

    @PostMapping("/coupons")
    public ResponseEntity<ApiResponse<AdminDto.CouponDetail>> createCoupon(
            @Valid @RequestBody AdminRequest.CreateCoupon request) {
        AdminDto.CouponDetail coupon = adminService.createCoupon(request);
        return ResponseEntity.ok(ApiResponse.success("Coupon created successfully", coupon));
    }

    @PutMapping("/coupons/{id}")
    public ResponseEntity<ApiResponse<AdminDto.CouponDetail>> updateCoupon(
            @PathVariable String id,
            @Valid @RequestBody AdminRequest.UpdateCoupon request) {
        AdminDto.CouponDetail coupon = adminService.updateCoupon(id, request);
        return ResponseEntity.ok(ApiResponse.success("Coupon updated successfully", coupon));
    }

    @DeleteMapping("/coupons/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCoupon(@PathVariable String id) {
        adminService.deleteCoupon(id);
        return ResponseEntity.ok(ApiResponse.success("Coupon deleted successfully", null));
    }

    // ===== USERS =====

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<Page<AdminDto.UserDetail>>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("asc")
            ? Sort.by(sortBy).ascending()
            : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<AdminDto.UserDetail> users = adminService.getAllUsers(pageable);
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<ApiResponse<AdminDto.UserDetail>> getUser(@PathVariable String id) {
        AdminDto.UserDetail user = adminService.getUser(id);
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<ApiResponse<AdminDto.UserDetail>> updateUser(
            @PathVariable String id,
            @Valid @RequestBody AdminRequest.UpdateUser request) {
        AdminDto.UserDetail user = adminService.updateUser(id, request);
        return ResponseEntity.ok(ApiResponse.success("User updated successfully", user));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable String id) {
        adminService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.success("User deleted successfully", null));
    }

    // ===== ORDERS =====

    @GetMapping("/orders")
    public ResponseEntity<ApiResponse<Page<AdminDto.OrderDetail>>> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<AdminDto.OrderDetail> orders = adminService.getAllOrders(pageable);
        return ResponseEntity.ok(ApiResponse.success(orders));
    }

    @GetMapping("/orders/status/{status}")
    public ResponseEntity<ApiResponse<List<AdminDto.OrderDetail>>> getOrdersByStatus(
            @PathVariable String status) {
        Order.OrderStatus orderStatus = Order.OrderStatus.valueOf(status.toUpperCase());
        List<AdminDto.OrderDetail> orders = adminService.getOrdersByStatus(orderStatus);
        return ResponseEntity.ok(ApiResponse.success(orders));
    }

    @GetMapping("/orders/{id}")
    public ResponseEntity<ApiResponse<AdminDto.OrderDetail>> getOrder(@PathVariable String id) {
        AdminDto.OrderDetail order = adminService.getOrder(id);
        return ResponseEntity.ok(ApiResponse.success(order));
    }

    @PatchMapping("/orders/{id}/status")
    public ResponseEntity<ApiResponse<AdminDto.OrderDetail>> updateOrderStatus(
            @PathVariable String id,
            @Valid @RequestBody AdminRequest.UpdateOrderStatus request) {
        AdminDto.OrderDetail order = adminService.updateOrderStatus(id, request);
        return ResponseEntity.ok(ApiResponse.success("Order status updated successfully", order));
    }

    @PatchMapping("/orders/{id}/payment")
    public ResponseEntity<ApiResponse<AdminDto.OrderDetail>> updateOrderPayment(
            @PathVariable String id,
            @Valid @RequestBody AdminRequest.UpdateOrderPayment request) {
        AdminDto.OrderDetail order = adminService.updateOrderPayment(id, request);
        return ResponseEntity.ok(ApiResponse.success("Order payment updated successfully", order));
    }

    // ===== REVIEWS =====

    @GetMapping("/reviews")
    public ResponseEntity<ApiResponse<Page<ReviewDto>>> getAllReviews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<ReviewDto> reviews = adminService.getAllReviews(pageable);
        return ResponseEntity.ok(ApiResponse.success(reviews));
    }

    @DeleteMapping("/reviews/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteReview(@PathVariable String id) {
        adminService.deleteReview(id);
        return ResponseEntity.ok(ApiResponse.success("Review deleted successfully", null));
    }
}
