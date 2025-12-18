package com.random_stuff.api.controller;

import com.random_stuff.api.dto.ApiResponse;
import com.random_stuff.api.entity.User;
import com.random_stuff.api.service.InventoryService;
import com.random_stuff.api.service.InventoryService.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/inventory")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<InventorySummary>> getSummary() {
        InventorySummary summary = inventoryService.getInventorySummary();
        return ResponseEntity.ok(ApiResponse.success(summary));
    }

    @GetMapping("/low-stock")
    public ResponseEntity<ApiResponse<List<LowStockProduct>>> getLowStock() {
        List<LowStockProduct> products = inventoryService.getLowStockProducts();
        return ResponseEntity.ok(ApiResponse.success(products));
    }

    @GetMapping("/history/{productId}")
    public ResponseEntity<ApiResponse<Page<StockMovementDto>>> getStockHistory(
            @PathVariable String productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<StockMovementDto> history = inventoryService.getStockHistory(
            productId, PageRequest.of(page, size));
        return ResponseEntity.ok(ApiResponse.success(history));
    }

    @GetMapping("/recent-movements")
    public ResponseEntity<ApiResponse<List<StockMovementDto>>> getRecentMovements(
            @RequestParam(defaultValue = "24") int hours) {
        List<StockMovementDto> movements = inventoryService.getRecentMovements(hours);
        return ResponseEntity.ok(ApiResponse.success(movements));
    }

    @PostMapping("/add-stock")
    public ResponseEntity<ApiResponse<StockMovementDto>> addStock(
            @Valid @RequestBody AddStockRequest request,
            @AuthenticationPrincipal User user) {
        StockMovementDto movement = inventoryService.addStock(
            request.getProductId(),
            request.getQuantity(),
            request.getReason(),
            user
        );
        return ResponseEntity.ok(ApiResponse.success("Stock added successfully", movement));
    }

    @PostMapping("/adjust-stock")
    public ResponseEntity<ApiResponse<StockMovementDto>> adjustStock(
            @Valid @RequestBody AdjustStockRequest request,
            @AuthenticationPrincipal User user) {
        StockMovementDto movement = inventoryService.adjustStock(
            request.getProductId(),
            request.getNewStock(),
            request.getReason(),
            user
        );
        return ResponseEntity.ok(ApiResponse.success("Stock adjusted successfully", movement));
    }

    @PostMapping("/bulk-update")
    public ResponseEntity<ApiResponse<BulkUpdateResult>> bulkUpdate(
            @Valid @RequestBody BulkUpdateRequest request,
            @AuthenticationPrincipal User user) {
        BulkUpdateResult result = inventoryService.bulkUpdateStock(request.getUpdates(), user);
        return ResponseEntity.ok(ApiResponse.success("Bulk update completed", result));
    }

    // ===== REQUEST DTOs =====

    @Data
    public static class AddStockRequest {
        @NotBlank(message = "Product ID is required")
        private String productId;

        @Min(value = 1, message = "Quantity must be at least 1")
        private int quantity;

        private String reason;
    }

    @Data
    public static class AdjustStockRequest {
        @NotBlank(message = "Product ID is required")
        private String productId;

        @Min(value = 0, message = "Stock cannot be negative")
        private int newStock;

        private String reason;
    }

    @Data
    public static class BulkUpdateRequest {
        private List<StockUpdateItem> updates;
    }
}
