package com.random_stuff.api.service;

import com.random_stuff.api.entity.Order;
import com.random_stuff.api.entity.OrderItem;
import com.random_stuff.api.entity.Product;
import com.random_stuff.api.entity.User;
import com.random_stuff.api.repository.OrderRepository;
import com.random_stuff.api.repository.ProductRepository;
import com.random_stuff.api.repository.ReviewRepository;
import com.random_stuff.api.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;

    @Transactional(readOnly = true)
    public DashboardAnalytics getDashboardAnalytics(int days) {
        LocalDateTime startDate = LocalDateTime.now().minusDays(days);
        LocalDateTime previousStartDate = startDate.minusDays(days);

        List<Order> currentOrders = orderRepository.findAll().stream()
            .filter(o -> o.getCreatedAt().isAfter(startDate))
            .toList();

        List<Order> previousOrders = orderRepository.findAll().stream()
            .filter(o -> o.getCreatedAt().isAfter(previousStartDate) &&
                        o.getCreatedAt().isBefore(startDate))
            .toList();

        // Revenue metrics
        BigDecimal currentRevenue = calculateRevenue(currentOrders);
        BigDecimal previousRevenue = calculateRevenue(previousOrders);
        double revenueGrowth = calculateGrowthRate(previousRevenue, currentRevenue);

        // Order metrics
        int currentOrderCount = currentOrders.size();
        int previousOrderCount = previousOrders.size();
        double orderGrowth = calculateGrowthRate(previousOrderCount, currentOrderCount);

        // Average order value
        BigDecimal currentAOV = currentOrderCount > 0
            ? currentRevenue.divide(BigDecimal.valueOf(currentOrderCount), 2, RoundingMode.HALF_UP)
            : BigDecimal.ZERO;
        BigDecimal previousAOV = previousOrderCount > 0
            ? previousRevenue.divide(BigDecimal.valueOf(previousOrderCount), 2, RoundingMode.HALF_UP)
            : BigDecimal.ZERO;
        double aovGrowth = calculateGrowthRate(previousAOV, currentAOV);

        // Customer metrics
        long newCustomers = userRepository.findAll().stream()
            .filter(u -> u.getCreatedAt().isAfter(startDate))
            .count();

        // Conversion funnel (simplified - in production, use proper event tracking)
        long totalProducts = productRepository.count();
        long productsWithOrders = currentOrders.stream()
            .flatMap(o -> o.getItems().stream())
            .filter(i -> i.getProduct() != null)
            .map(i -> i.getProduct().getId())
            .distinct()
            .count();

        return DashboardAnalytics.builder()
            .totalRevenue(currentRevenue)
            .revenueGrowth(revenueGrowth)
            .totalOrders(currentOrderCount)
            .orderGrowth(orderGrowth)
            .averageOrderValue(currentAOV)
            .aovGrowth(aovGrowth)
            .newCustomers((int) newCustomers)
            .conversionRate(totalProducts > 0 ? (double) productsWithOrders / totalProducts * 100 : 0)
            .build();
    }

    @Transactional(readOnly = true)
    public List<SalesDataPoint> getSalesTrend(int days) {
        LocalDateTime startDate = LocalDateTime.now().minusDays(days);

        Map<LocalDate, List<Order>> ordersByDate = orderRepository.findAll().stream()
            .filter(o -> o.getCreatedAt().isAfter(startDate))
            .collect(Collectors.groupingBy(o -> o.getCreatedAt().toLocalDate()));

        List<SalesDataPoint> dataPoints = new ArrayList<>();
        LocalDate current = startDate.toLocalDate();
        LocalDate end = LocalDate.now();

        while (!current.isAfter(end)) {
            List<Order> dayOrders = ordersByDate.getOrDefault(current, List.of());
            BigDecimal revenue = calculateRevenue(dayOrders);
            int orderCount = dayOrders.size();

            dataPoints.add(SalesDataPoint.builder()
                .date(current.toString())
                .revenue(revenue)
                .orders(orderCount)
                .build());

            current = current.plusDays(1);
        }

        return dataPoints;
    }

    @Transactional(readOnly = true)
    public List<CategoryRevenue> getRevenueByCategory() {
        Map<String, BigDecimal> categoryRevenue = new HashMap<>();
        Map<String, Integer> categoryOrders = new HashMap<>();

        for (Order order : orderRepository.findAll()) {
            if (!order.isPaid()) continue;

            for (OrderItem item : order.getItems()) {
                if (item.getProduct() == null || item.getProduct().getCategory() == null) continue;

                String categoryName = item.getProduct().getCategory().getName();
                BigDecimal itemTotal = item.getPrice()
                    .multiply(BigDecimal.valueOf(item.getQuantity()));

                categoryRevenue.merge(categoryName, itemTotal, BigDecimal::add);
                categoryOrders.merge(categoryName, item.getQuantity(), Integer::sum);
            }
        }

        BigDecimal totalRevenue = categoryRevenue.values().stream()
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        return categoryRevenue.entrySet().stream()
            .map(entry -> CategoryRevenue.builder()
                .category(entry.getKey())
                .revenue(entry.getValue())
                .percentage(totalRevenue.compareTo(BigDecimal.ZERO) > 0
                    ? entry.getValue().divide(totalRevenue, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100)).doubleValue()
                    : 0)
                .orders(categoryOrders.getOrDefault(entry.getKey(), 0))
                .build())
            .sorted((a, b) -> b.getRevenue().compareTo(a.getRevenue()))
            .toList();
    }

    @Transactional(readOnly = true)
    public List<TopProduct> getTopProducts(int limit) {
        Map<String, ProductStats> productStats = new HashMap<>();

        for (Order order : orderRepository.findAll()) {
            if (!order.isPaid()) continue;

            for (OrderItem item : order.getItems()) {
                if (item.getProduct() == null) continue;

                String productId = item.getProduct().getId();
                ProductStats stats = productStats.computeIfAbsent(productId, k ->
                    new ProductStats(item.getProduct()));

                stats.addSale(item.getQuantity(), item.getPrice());
            }
        }

        return productStats.values().stream()
            .sorted((a, b) -> b.revenue.compareTo(a.revenue))
            .limit(limit)
            .map(stats -> TopProduct.builder()
                .id(stats.product.getId())
                .name(stats.product.getName())
                .image(stats.product.getImages() != null && !stats.product.getImages().isEmpty()
                    ? stats.product.getImages().get(0) : null)
                .unitsSold(stats.unitsSold)
                .revenue(stats.revenue)
                .rating(stats.product.getRating())
                .build())
            .toList();
    }

    @Transactional(readOnly = true)
    public OrderStatusBreakdown getOrderStatusBreakdown() {
        Map<String, Long> statusCounts = orderRepository.findAll().stream()
            .collect(Collectors.groupingBy(
                o -> o.getStatus().name(),
                Collectors.counting()
            ));

        long total = statusCounts.values().stream().mapToLong(Long::longValue).sum();

        Map<String, Double> statusPercentages = new HashMap<>();
        for (Map.Entry<String, Long> entry : statusCounts.entrySet()) {
            double percentage = total > 0 ? (double) entry.getValue() / total * 100 : 0;
            statusPercentages.put(entry.getKey(), Math.round(percentage * 100) / 100.0);
        }

        return OrderStatusBreakdown.builder()
            .statusCounts(statusCounts)
            .statusPercentages(statusPercentages)
            .total(total)
            .build();
    }

    @Transactional(readOnly = true)
    public CustomerAnalytics getCustomerAnalytics() {
        List<User> users = userRepository.findAll();
        List<Order> orders = orderRepository.findAll();

        // Calculate repeat customers
        Map<String, Long> ordersPerUser = orders.stream()
            .collect(Collectors.groupingBy(o -> o.getUser().getId(), Collectors.counting()));

        long repeatCustomers = ordersPerUser.values().stream()
            .filter(count -> count > 1)
            .count();

        long totalCustomersWithOrders = ordersPerUser.size();

        // Customer lifetime value
        Map<String, BigDecimal> revenuePerUser = new HashMap<>();
        for (Order order : orders) {
            if (order.isPaid()) {
                revenuePerUser.merge(order.getUser().getId(), order.getTotal(), BigDecimal::add);
            }
        }

        BigDecimal avgLifetimeValue = revenuePerUser.isEmpty()
            ? BigDecimal.ZERO
            : revenuePerUser.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(revenuePerUser.size()), 2, RoundingMode.HALF_UP);

        // Top customers
        List<TopCustomer> topCustomers = revenuePerUser.entrySet().stream()
            .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
            .limit(10)
            .map(entry -> {
                User user = userRepository.findById(entry.getKey()).orElse(null);
                return TopCustomer.builder()
                    .id(entry.getKey())
                    .name(user != null ? user.getName() : "Unknown")
                    .email(user != null ? user.getEmail() : "")
                    .totalSpent(entry.getValue())
                    .orderCount(ordersPerUser.getOrDefault(entry.getKey(), 0L).intValue())
                    .build();
            })
            .toList();

        return CustomerAnalytics.builder()
            .totalCustomers(users.size())
            .customersWithOrders((int) totalCustomersWithOrders)
            .repeatCustomers((int) repeatCustomers)
            .repeatRate(totalCustomersWithOrders > 0
                ? (double) repeatCustomers / totalCustomersWithOrders * 100 : 0)
            .averageLifetimeValue(avgLifetimeValue)
            .topCustomers(topCustomers)
            .build();
    }

    // ===== HELPER METHODS =====

    private BigDecimal calculateRevenue(List<Order> orders) {
        return orders.stream()
            .filter(Order::isPaid)
            .map(Order::getTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private double calculateGrowthRate(BigDecimal previous, BigDecimal current) {
        if (previous.compareTo(BigDecimal.ZERO) == 0) {
            return current.compareTo(BigDecimal.ZERO) > 0 ? 100 : 0;
        }
        return current.subtract(previous)
            .divide(previous, 4, RoundingMode.HALF_UP)
            .multiply(BigDecimal.valueOf(100))
            .doubleValue();
    }

    private double calculateGrowthRate(int previous, int current) {
        if (previous == 0) {
            return current > 0 ? 100 : 0;
        }
        return (double) (current - previous) / previous * 100;
    }

    // ===== HELPER CLASS =====
    private static class ProductStats {
        Product product;
        int unitsSold = 0;
        BigDecimal revenue = BigDecimal.ZERO;

        ProductStats(Product product) {
            this.product = product;
        }

        void addSale(int quantity, BigDecimal price) {
            this.unitsSold += quantity;
            this.revenue = this.revenue.add(price.multiply(BigDecimal.valueOf(quantity)));
        }
    }

    // ===== DTOs =====

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DashboardAnalytics {
        private BigDecimal totalRevenue;
        private double revenueGrowth;
        private int totalOrders;
        private double orderGrowth;
        private BigDecimal averageOrderValue;
        private double aovGrowth;
        private int newCustomers;
        private double conversionRate;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SalesDataPoint {
        private String date;
        private BigDecimal revenue;
        private int orders;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryRevenue {
        private String category;
        private BigDecimal revenue;
        private double percentage;
        private int orders;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopProduct {
        private String id;
        private String name;
        private String image;
        private int unitsSold;
        private BigDecimal revenue;
        private double rating;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderStatusBreakdown {
        private Map<String, Long> statusCounts;
        private Map<String, Double> statusPercentages;
        private long total;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CustomerAnalytics {
        private int totalCustomers;
        private int customersWithOrders;
        private int repeatCustomers;
        private double repeatRate;
        private BigDecimal averageLifetimeValue;
        private List<TopCustomer> topCustomers;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopCustomer {
        private String id;
        private String name;
        private String email;
        private BigDecimal totalSpent;
        private int orderCount;
    }
}
