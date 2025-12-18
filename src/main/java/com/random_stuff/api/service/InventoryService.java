package com.random_stuff.api.service;

import com.random_stuff.api.dto.AdminDto;
import com.random_stuff.api.entity.Product;
import com.random_stuff.api.entity.StockMovement;
import com.random_stuff.api.entity.User;
import com.random_stuff.api.exception.BadRequestException;
import com.random_stuff.api.exception.ResourceNotFoundException;
import com.random_stuff.api.repository.ProductRepository;
import com.random_stuff.api.repository.StockMovementRepository;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {

    private final ProductRepository productRepository;
    private final StockMovementRepository stockMovementRepository;
    private final NotificationService notificationService;

    @Value("${inventory.low-stock-threshold:10}")
    private int lowStockThreshold;

    @Value("${inventory.critical-stock-threshold:3}")
    private int criticalStockThreshold;

    // ===== STOCK OPERATIONS =====

    @Transactional
    public StockMovementDto addStock(String productId, int quantity, String reason, User performedBy) {
        if (quantity <= 0) {
            throw new BadRequestException("Quantity must be positive");
        }

        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        int previousStock = product.getStock();
        int newStock = previousStock + quantity;
        product.setStock(newStock);
        productRepository.save(product);

        StockMovement movement = StockMovement.builder()
            .product(product)
            .type(StockMovement.MovementType.RESTOCK)
            .quantity(quantity)
            .previousStock(previousStock)
            .newStock(newStock)
            .reason(reason)
            .performedBy(performedBy)
            .build();

        movement = stockMovementRepository.save(movement);

        log.info("Added {} units to product {}: {} -> {}", quantity, product.getName(), previousStock, newStock);

        return StockMovementDto.fromEntity(movement);
    }

    @Transactional
    public StockMovementDto reduceStock(String productId, int quantity, String reason,
                                        StockMovement.MovementType type, String referenceId, User performedBy) {
        if (quantity <= 0) {
            throw new BadRequestException("Quantity must be positive");
        }

        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        int previousStock = product.getStock();
        int newStock = previousStock - quantity;

        if (newStock < 0) {
            throw new BadRequestException("Insufficient stock. Available: " + previousStock);
        }

        product.setStock(newStock);
        productRepository.save(product);

        StockMovement movement = StockMovement.builder()
            .product(product)
            .type(type)
            .quantity(-quantity)
            .previousStock(previousStock)
            .newStock(newStock)
            .reason(reason)
            .referenceId(referenceId)
            .performedBy(performedBy)
            .build();

        movement = stockMovementRepository.save(movement);

        // Check for low stock alerts
        checkStockLevel(product);

        log.info("Reduced {} units from product {}: {} -> {}", quantity, product.getName(), previousStock, newStock);

        return StockMovementDto.fromEntity(movement);
    }

    @Transactional
    public StockMovementDto adjustStock(String productId, int newStockLevel, String reason, User performedBy) {
        if (newStockLevel < 0) {
            throw new BadRequestException("Stock level cannot be negative");
        }

        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        int previousStock = product.getStock();
        int difference = newStockLevel - previousStock;

        product.setStock(newStockLevel);
        productRepository.save(product);

        StockMovement movement = StockMovement.builder()
            .product(product)
            .type(StockMovement.MovementType.ADJUSTMENT)
            .quantity(difference)
            .previousStock(previousStock)
            .newStock(newStockLevel)
            .reason(reason)
            .performedBy(performedBy)
            .build();

        movement = stockMovementRepository.save(movement);

        // Check for low stock alerts
        checkStockLevel(product);

        log.info("Adjusted stock for product {}: {} -> {} (reason: {})",
            product.getName(), previousStock, newStockLevel, reason);

        return StockMovementDto.fromEntity(movement);
    }

    @Transactional
    public void recordSale(String productId, int quantity, String orderId) {
        reduceStock(productId, quantity, "Sale - Order #" + orderId.substring(0, 8),
            StockMovement.MovementType.SALE, orderId, null);
    }

    @Transactional
    public void recordReturn(String productId, int quantity, String orderId, User performedBy) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        int previousStock = product.getStock();
        int newStock = previousStock + quantity;
        product.setStock(newStock);
        productRepository.save(product);

        StockMovement movement = StockMovement.builder()
            .product(product)
            .type(StockMovement.MovementType.RETURN)
            .quantity(quantity)
            .previousStock(previousStock)
            .newStock(newStock)
            .reason("Return from Order #" + orderId.substring(0, 8))
            .referenceId(orderId)
            .performedBy(performedBy)
            .build();

        stockMovementRepository.save(movement);
        log.info("Returned {} units to product {}", quantity, product.getName());
    }

    // ===== STOCK QUERIES =====

    @Transactional(readOnly = true)
    public List<LowStockProduct> getLowStockProducts() {
        return productRepository.findByActiveTrue().stream()
            .filter(p -> p.getStock() <= lowStockThreshold)
            .map(p -> LowStockProduct.builder()
                .id(p.getId())
                .name(p.getName())
                .image(p.getImages() != null && !p.getImages().isEmpty() ? p.getImages().get(0) : null)
                .currentStock(p.getStock())
                .threshold(lowStockThreshold)
                .status(p.getStock() == 0 ? "OUT_OF_STOCK" :
                       p.getStock() <= criticalStockThreshold ? "CRITICAL" : "LOW")
                .build())
            .sorted((a, b) -> Integer.compare(a.getCurrentStock(), b.getCurrentStock()))
            .toList();
    }

    @Transactional(readOnly = true)
    public Page<StockMovementDto> getStockHistory(String productId, Pageable pageable) {
        return stockMovementRepository.findByProductIdOrderByCreatedAtDesc(productId, pageable)
            .map(StockMovementDto::fromEntity);
    }

    @Transactional(readOnly = true)
    public List<StockMovementDto> getRecentMovements(int hours) {
        LocalDateTime startDate = LocalDateTime.now().minusHours(hours);
        return stockMovementRepository.findRecentMovements(startDate).stream()
            .map(StockMovementDto::fromEntity)
            .toList();
    }

    @Transactional(readOnly = true)
    public InventorySummary getInventorySummary() {
        List<Product> products = productRepository.findByActiveTrue();

        long totalProducts = products.size();
        long outOfStock = products.stream().filter(p -> p.getStock() == 0).count();
        long lowStock = products.stream()
            .filter(p -> p.getStock() > 0 && p.getStock() <= lowStockThreshold).count();
        long inStock = products.stream().filter(p -> p.getStock() > lowStockThreshold).count();

        int totalUnits = products.stream().mapToInt(Product::getStock).sum();

        return InventorySummary.builder()
            .totalProducts(totalProducts)
            .outOfStock(outOfStock)
            .lowStock(lowStock)
            .inStock(inStock)
            .totalUnits(totalUnits)
            .lowStockThreshold(lowStockThreshold)
            .criticalStockThreshold(criticalStockThreshold)
            .build();
    }

    // ===== BULK OPERATIONS =====

    @Transactional
    public BulkUpdateResult bulkUpdateStock(List<StockUpdateItem> updates, User performedBy) {
        int success = 0;
        int failed = 0;
        List<String> errors = new java.util.ArrayList<>();

        for (StockUpdateItem update : updates) {
            try {
                adjustStock(update.getProductId(), update.getNewStock(), update.getReason(), performedBy);
                success++;
            } catch (Exception e) {
                failed++;
                errors.add(update.getProductId() + ": " + e.getMessage());
            }
        }

        return BulkUpdateResult.builder()
            .success(success)
            .failed(failed)
            .errors(errors)
            .build();
    }

    // ===== SCHEDULED TASKS =====

    @Scheduled(fixedRateString = "${inventory.check-interval-hours:1}000")
    @Transactional(readOnly = true)
    public void checkInventoryLevels() {
        log.debug("Running scheduled inventory check...");

        List<Product> lowStockProducts = productRepository.findByActiveTrue().stream()
            .filter(p -> p.getStock() <= lowStockThreshold && p.getStock() > 0)
            .toList();

        List<Product> outOfStockProducts = productRepository.findByActiveTrue().stream()
            .filter(p -> p.getStock() == 0)
            .toList();

        for (Product product : outOfStockProducts) {
            notificationService.notifyOutOfStock(product);
        }

        for (Product product : lowStockProducts) {
            if (product.getStock() <= criticalStockThreshold) {
                notificationService.notifyLowStock(product);
            }
        }

        if (!lowStockProducts.isEmpty() || !outOfStockProducts.isEmpty()) {
            log.warn("Inventory alert: {} low stock, {} out of stock",
                lowStockProducts.size(), outOfStockProducts.size());
        }
    }

    // ===== HELPER METHODS =====

    private void checkStockLevel(Product product) {
        if (product.getStock() == 0) {
            notificationService.notifyOutOfStock(product);
        } else if (product.getStock() <= criticalStockThreshold) {
            notificationService.notifyLowStock(product);
        }
    }

    // ===== DTOs =====

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StockMovementDto {
        private String id;
        private String productId;
        private String productName;
        private String type;
        private int quantity;
        private int previousStock;
        private int newStock;
        private String reason;
        private String referenceId;
        private String performedBy;
        private LocalDateTime createdAt;

        public static StockMovementDto fromEntity(StockMovement movement) {
            return StockMovementDto.builder()
                .id(movement.getId())
                .productId(movement.getProduct().getId())
                .productName(movement.getProduct().getName())
                .type(movement.getType().name())
                .quantity(movement.getQuantity())
                .previousStock(movement.getPreviousStock())
                .newStock(movement.getNewStock())
                .reason(movement.getReason())
                .referenceId(movement.getReferenceId())
                .performedBy(movement.getPerformedBy() != null ? movement.getPerformedBy().getName() : null)
                .createdAt(movement.getCreatedAt())
                .build();
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LowStockProduct {
        private String id;
        private String name;
        private String image;
        private int currentStock;
        private int threshold;
        private String status;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InventorySummary {
        private long totalProducts;
        private long outOfStock;
        private long lowStock;
        private long inStock;
        private int totalUnits;
        private int lowStockThreshold;
        private int criticalStockThreshold;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StockUpdateItem {
        private String productId;
        private int newStock;
        private String reason;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BulkUpdateResult {
        private int success;
        private int failed;
        private List<String> errors;
    }
}
