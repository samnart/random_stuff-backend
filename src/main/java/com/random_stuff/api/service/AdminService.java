package com.random_stuff.api.service;

import com.random_stuff.api.dto.AdminDto;
import com.random_stuff.api.dto.AdminRequest;
import com.random_stuff.api.entity.*;
import com.random_stuff.api.exception.BadRequestException;
import com.random_stuff.api.exception.ResourceNotFoundException;
import com.random_stuff.api.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final CouponRepository couponRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final ReviewRepository reviewRepository;
    private final PasswordEncoder passwordEncoder;

    // ===== DASHBOARD =====

    @Transactional(readOnly = true)
    public AdminDto.DashboardStats getDashboardStats() {
        long totalUsers = userRepository.count();
        long totalProducts = productRepository.count();
        long totalOrders = orderRepository.count();
        long totalCategories = categoryRepository.count();
        long activeCoupons = couponRepository.findAll().stream()
            .filter(Coupon::isValid).count();

        BigDecimal totalRevenue = orderRepository.findAll().stream()
            .filter(Order::isPaid)
            .map(Order::getTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        long pendingOrders = orderRepository.findByStatus(Order.OrderStatus.PROCESSING).size();
        long lowStockProducts = productRepository.findByActiveTrue().stream()
            .filter(p -> p.getStock() < 10).count();

        List<AdminDto.DashboardStats.RecentOrder> recentOrders = orderRepository
            .findAllByOrderByCreatedAtDesc(PageRequest.of(0, 5))
            .map(order -> AdminDto.DashboardStats.RecentOrder.builder()
                .id(order.getId())
                .customerName(order.getUser().getName())
                .total(order.getTotal())
                .status(order.getStatus().name())
                .createdAt(order.getCreatedAt())
                .build())
            .toList();

        return AdminDto.DashboardStats.builder()
            .totalUsers(totalUsers)
            .totalProducts(totalProducts)
            .totalOrders(totalOrders)
            .totalCategories(totalCategories)
            .activeCoupons(activeCoupons)
            .totalRevenue(totalRevenue)
            .pendingOrders(pendingOrders)
            .lowStockProducts(lowStockProducts)
            .recentOrders(recentOrders)
            .topProducts(List.of()) // TODO: Implement top products query
            .build();
    }

    // ===== PRODUCTS =====

    @Transactional(readOnly = true)
    public Page<AdminDto.ProductDetail> getAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable)
            .map(AdminDto.ProductDetail::fromEntity);
    }

    @Transactional(readOnly = true)
    public AdminDto.ProductDetail getProduct(String id) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
        return AdminDto.ProductDetail.fromEntity(product);
    }

    @Transactional
    public AdminDto.ProductDetail createProduct(AdminRequest.CreateProduct request) {
        Product product = Product.builder()
            .name(request.getName())
            .price(request.getPrice())
            .originalPrice(request.getOriginalPrice())
            .description(request.getDescription())
            .images(request.getImages() != null ? request.getImages() : List.of())
            .colors(request.getColors() != null ? request.getColors() : List.of())
            .sizes(request.getSizes() != null ? request.getSizes() : List.of())
            .badge(request.getBadge())
            .featured(request.isFeatured())
            .stock(request.getStock())
            .active(true)
            .build();

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.getCategoryId()));
            product.setCategory(category);
        }

        if (request.getSpecifications() != null) {
            for (AdminRequest.ProductSpecDto specDto : request.getSpecifications()) {
                ProductSpecification spec = ProductSpecification.builder()
                    .label(specDto.getLabel())
                    .value(specDto.getValue())
                    .build();
                product.addSpecification(spec);
            }
        }

        product = productRepository.save(product);
        return AdminDto.ProductDetail.fromEntity(product);
    }

    @Transactional
    public AdminDto.ProductDetail updateProduct(String id, AdminRequest.UpdateProduct request) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

        if (request.getName() != null) product.setName(request.getName());
        if (request.getPrice() != null) product.setPrice(request.getPrice());
        if (request.getOriginalPrice() != null) product.setOriginalPrice(request.getOriginalPrice());
        if (request.getDescription() != null) product.setDescription(request.getDescription());
        if (request.getImages() != null) product.setImages(request.getImages());
        if (request.getColors() != null) product.setColors(request.getColors());
        if (request.getSizes() != null) product.setSizes(request.getSizes());
        if (request.getBadge() != null) product.setBadge(request.getBadge());
        if (request.getFeatured() != null) product.setFeatured(request.getFeatured());
        if (request.getActive() != null) product.setActive(request.getActive());
        if (request.getStock() != null) product.setStock(request.getStock());

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.getCategoryId()));
            product.setCategory(category);
        }

        if (request.getSpecifications() != null) {
            product.getSpecifications().clear();
            for (AdminRequest.ProductSpecDto specDto : request.getSpecifications()) {
                ProductSpecification spec = ProductSpecification.builder()
                    .label(specDto.getLabel())
                    .value(specDto.getValue())
                    .build();
                product.addSpecification(spec);
            }
        }

        product = productRepository.save(product);
        return AdminDto.ProductDetail.fromEntity(product);
    }

    @Transactional
    public void deleteProduct(String id) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
        // Soft delete - just mark as inactive
        product.setActive(false);
        productRepository.save(product);
    }

    @Transactional
    public void hardDeleteProduct(String id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product", "id", id);
        }
        productRepository.deleteById(id);
    }

    // ===== CATEGORIES =====

    @Transactional(readOnly = true)
    public List<AdminDto.CategoryDetail> getAllCategories() {
        return categoryRepository.findAll(Sort.by("displayOrder")).stream()
            .map(AdminDto.CategoryDetail::fromEntity)
            .toList();
    }

    @Transactional(readOnly = true)
    public AdminDto.CategoryDetail getCategory(String id) {
        Category category = categoryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));
        return AdminDto.CategoryDetail.fromEntity(category);
    }

    @Transactional
    public AdminDto.CategoryDetail createCategory(AdminRequest.CreateCategory request) {
        if (categoryRepository.existsBySlug(request.getSlug())) {
            throw new BadRequestException("Category with slug '" + request.getSlug() + "' already exists");
        }

        Category category = Category.builder()
            .name(request.getName())
            .slug(request.getSlug())
            .icon(request.getIcon())
            .description(request.getDescription())
            .displayOrder(request.getDisplayOrder())
            .active(request.isActive())
            .build();

        category = categoryRepository.save(category);
        return AdminDto.CategoryDetail.fromEntity(category);
    }

    @Transactional
    public AdminDto.CategoryDetail updateCategory(String id, AdminRequest.UpdateCategory request) {
        Category category = categoryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));

        if (request.getName() != null) category.setName(request.getName());
        if (request.getSlug() != null) {
            if (!category.getSlug().equals(request.getSlug()) && categoryRepository.existsBySlug(request.getSlug())) {
                throw new BadRequestException("Category with slug '" + request.getSlug() + "' already exists");
            }
            category.setSlug(request.getSlug());
        }
        if (request.getIcon() != null) category.setIcon(request.getIcon());
        if (request.getDescription() != null) category.setDescription(request.getDescription());
        if (request.getDisplayOrder() != null) category.setDisplayOrder(request.getDisplayOrder());
        if (request.getActive() != null) category.setActive(request.getActive());

        category = categoryRepository.save(category);
        return AdminDto.CategoryDetail.fromEntity(category);
    }

    @Transactional
    public void deleteCategory(String id) {
        Category category = categoryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));

        if (category.getProducts() != null && !category.getProducts().isEmpty()) {
            throw new BadRequestException("Cannot delete category with associated products");
        }

        categoryRepository.delete(category);
    }

    // ===== COUPONS =====

    @Transactional(readOnly = true)
    public List<AdminDto.CouponDetail> getAllCoupons() {
        return couponRepository.findAll().stream()
            .map(AdminDto.CouponDetail::fromEntity)
            .toList();
    }

    @Transactional(readOnly = true)
    public AdminDto.CouponDetail getCoupon(String id) {
        Coupon coupon = couponRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Coupon", "id", id));
        return AdminDto.CouponDetail.fromEntity(coupon);
    }

    @Transactional
    public AdminDto.CouponDetail createCoupon(AdminRequest.CreateCoupon request) {
        if (couponRepository.existsByCodeIgnoreCase(request.getCode())) {
            throw new BadRequestException("Coupon with code '" + request.getCode() + "' already exists");
        }

        Coupon coupon = Coupon.builder()
            .code(request.getCode().toUpperCase())
            .type(Coupon.DiscountType.valueOf(request.getType()))
            .discount(request.getDiscount())
            .minOrder(request.getMinOrder())
            .maxDiscount(request.getMaxDiscount())
            .usageLimit(request.getUsageLimit())
            .validFrom(request.getValidFrom())
            .validUntil(request.getValidUntil())
            .active(request.isActive())
            .build();

        coupon = couponRepository.save(coupon);
        return AdminDto.CouponDetail.fromEntity(coupon);
    }

    @Transactional
    public AdminDto.CouponDetail updateCoupon(String id, AdminRequest.UpdateCoupon request) {
        Coupon coupon = couponRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Coupon", "id", id));

        if (request.getCode() != null) {
            if (!coupon.getCode().equalsIgnoreCase(request.getCode()) &&
                couponRepository.existsByCodeIgnoreCase(request.getCode())) {
                throw new BadRequestException("Coupon with code '" + request.getCode() + "' already exists");
            }
            coupon.setCode(request.getCode().toUpperCase());
        }
        if (request.getType() != null) coupon.setType(Coupon.DiscountType.valueOf(request.getType()));
        if (request.getDiscount() != null) coupon.setDiscount(request.getDiscount());
        if (request.getMinOrder() != null) coupon.setMinOrder(request.getMinOrder());
        if (request.getMaxDiscount() != null) coupon.setMaxDiscount(request.getMaxDiscount());
        if (request.getUsageLimit() != null) coupon.setUsageLimit(request.getUsageLimit());
        if (request.getValidFrom() != null) coupon.setValidFrom(request.getValidFrom());
        if (request.getValidUntil() != null) coupon.setValidUntil(request.getValidUntil());
        if (request.getActive() != null) coupon.setActive(request.getActive());

        coupon = couponRepository.save(coupon);
        return AdminDto.CouponDetail.fromEntity(coupon);
    }

    @Transactional
    public void deleteCoupon(String id) {
        if (!couponRepository.existsById(id)) {
            throw new ResourceNotFoundException("Coupon", "id", id);
        }
        couponRepository.deleteById(id);
    }

    // ===== USERS =====

    @Transactional(readOnly = true)
    public Page<AdminDto.UserDetail> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
            .map(AdminDto.UserDetail::fromEntity);
    }

    @Transactional(readOnly = true)
    public AdminDto.UserDetail getUser(String id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        return AdminDto.UserDetail.fromEntity(user);
    }

    @Transactional
    public AdminDto.UserDetail updateUser(String id, AdminRequest.UpdateUser request) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        if (request.getName() != null) user.setName(request.getName());
        if (request.getEmail() != null) {
            if (!user.getEmail().equalsIgnoreCase(request.getEmail()) &&
                userRepository.existsByEmail(request.getEmail().toLowerCase())) {
                throw new BadRequestException("Email already in use");
            }
            user.setEmail(request.getEmail().toLowerCase());
        }
        if (request.getPhone() != null) user.setPhone(request.getPhone());
        if (request.getAvatar() != null) user.setAvatar(request.getAvatar());
        if (request.getRole() != null) user.setRole(User.Role.valueOf(request.getRole()));
        if (request.getEmailVerified() != null) user.setEmailVerified(request.getEmailVerified());

        user = userRepository.save(user);
        return AdminDto.UserDetail.fromEntity(user);
    }

    @Transactional
    public void deleteUser(String id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        if (user.getRole() == User.Role.ADMIN) {
            long adminCount = userRepository.findAll().stream()
                .filter(u -> u.getRole() == User.Role.ADMIN).count();
            if (adminCount <= 1) {
                throw new BadRequestException("Cannot delete the last admin user");
            }
        }

        userRepository.delete(user);
    }

    // ===== ORDERS =====

    @Transactional(readOnly = true)
    public Page<AdminDto.OrderDetail> getAllOrders(Pageable pageable) {
        return orderRepository.findAllByOrderByCreatedAtDesc(pageable)
            .map(AdminDto.OrderDetail::fromEntity);
    }

    @Transactional(readOnly = true)
    public List<AdminDto.OrderDetail> getOrdersByStatus(Order.OrderStatus status) {
        return orderRepository.findByStatus(status).stream()
            .map(AdminDto.OrderDetail::fromEntity)
            .toList();
    }

    @Transactional(readOnly = true)
    public AdminDto.OrderDetail getOrder(String id) {
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Order", "id", id));
        return AdminDto.OrderDetail.fromEntity(order);
    }

    @Transactional
    public AdminDto.OrderDetail updateOrderStatus(String id, AdminRequest.UpdateOrderStatus request) {
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Order", "id", id));

        Order.OrderStatus newStatus = Order.OrderStatus.valueOf(request.getStatus());

        // Handle stock restoration on cancellation
        if (newStatus == Order.OrderStatus.CANCELLED && order.getStatus() != Order.OrderStatus.CANCELLED) {
            for (OrderItem item : order.getItems()) {
                if (item.getProduct() != null) {
                    Product product = item.getProduct();
                    product.setStock(product.getStock() + item.getQuantity());
                    productRepository.save(product);
                }
            }
        }

        order.setStatus(newStatus);
        if (request.getNotes() != null) order.setNotes(request.getNotes());

        order = orderRepository.save(order);
        return AdminDto.OrderDetail.fromEntity(order);
    }

    @Transactional
    public AdminDto.OrderDetail updateOrderPayment(String id, AdminRequest.UpdateOrderPayment request) {
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Order", "id", id));

        order.setPaid(request.isPaid());
        if (request.isPaid()) {
            order.setPaidAt(LocalDateTime.now());
        } else {
            order.setPaidAt(null);
        }
        if (request.getNotes() != null) order.setNotes(request.getNotes());

        order = orderRepository.save(order);
        return AdminDto.OrderDetail.fromEntity(order);
    }

    // ===== REVIEWS =====

    @Transactional(readOnly = true)
    public Page<com.random_stuff.api.dto.ReviewDto> getAllReviews(Pageable pageable) {
        return reviewRepository.findAll(pageable)
            .map(com.random_stuff.api.dto.ReviewDto::fromEntity);
    }

    @Transactional
    public void deleteReview(String id) {
        Review review = reviewRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Review", "id", id));

        Product product = review.getProduct();
        reviewRepository.delete(review);

        // Update product rating
        if (product != null) {
            product.updateRating();
            productRepository.save(product);
        }
    }
}
