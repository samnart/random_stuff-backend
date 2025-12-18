package com.random_stuff.api.controller;

import com.random_stuff.api.dto.ApiResponse;
import com.random_stuff.api.service.AnalyticsService;
import com.random_stuff.api.service.AnalyticsService.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/analytics")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<DashboardAnalytics>> getDashboardAnalytics(
            @RequestParam(defaultValue = "30") int days) {
        DashboardAnalytics analytics = analyticsService.getDashboardAnalytics(days);
        return ResponseEntity.ok(ApiResponse.success(analytics));
    }

    @GetMapping("/sales-trend")
    public ResponseEntity<ApiResponse<List<SalesDataPoint>>> getSalesTrend(
            @RequestParam(defaultValue = "30") int days) {
        List<SalesDataPoint> trend = analyticsService.getSalesTrend(days);
        return ResponseEntity.ok(ApiResponse.success(trend));
    }

    @GetMapping("/revenue-by-category")
    public ResponseEntity<ApiResponse<List<CategoryRevenue>>> getRevenueByCategory() {
        List<CategoryRevenue> data = analyticsService.getRevenueByCategory();
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @GetMapping("/top-products")
    public ResponseEntity<ApiResponse<List<TopProduct>>> getTopProducts(
            @RequestParam(defaultValue = "10") int limit) {
        List<TopProduct> products = analyticsService.getTopProducts(limit);
        return ResponseEntity.ok(ApiResponse.success(products));
    }

    @GetMapping("/order-status")
    public ResponseEntity<ApiResponse<OrderStatusBreakdown>> getOrderStatusBreakdown() {
        OrderStatusBreakdown breakdown = analyticsService.getOrderStatusBreakdown();
        return ResponseEntity.ok(ApiResponse.success(breakdown));
    }

    @GetMapping("/customers")
    public ResponseEntity<ApiResponse<CustomerAnalytics>> getCustomerAnalytics() {
        CustomerAnalytics analytics = analyticsService.getCustomerAnalytics();
        return ResponseEntity.ok(ApiResponse.success(analytics));
    }
}
