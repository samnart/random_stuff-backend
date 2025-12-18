package com.random_stuff.api.config;
 
import com.random_stuff.api.entity.*;
import com.random_stuff.api.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
 
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
 
@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {
 
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final CouponRepository couponRepository;
    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;
    private final PasswordEncoder passwordEncoder;
 
    @Override
    @Transactional
    public void run(String... args) {
        if (categoryRepository.count() > 0) {
            log.info("Database already seeded, skipping...");
            return;
        }
 
        log.info("Seeding database...");
        seedCategories();
        seedProducts();
        seedCoupons();
        seedTestUser();
        log.info("Database seeding complete!");
    }
 
    private void seedCategories() {
        List<Category> categories = Arrays.asList(
            createCategory("electronics", "Electronics", "Laptop", 1),
            createCategory("clothing", "Clothing", "Shirt", 2),
            createCategory("home", "Home & Garden", "Home", 3),
            createCategory("sports", "Sports & Outdoors", "Dumbbell", 4),
            createCategory("books", "Books", "Book", 5),
            createCategory("toys", "Toys & Games", "Gamepad", 6)
        );
        categoryRepository.saveAll(categories);
        log.info("Seeded {} categories", categories.size());
    }
 
    private Category createCategory(String slug, String name, String icon, int order) {
        Category category = new Category();
        category.setSlug(slug);
        category.setName(name);
        category.setIcon(icon);
        category.setDisplayOrder(order);
        return category;
    }
 
    private void seedProducts() {
        Category electronics = categoryRepository.findBySlug("electronics").orElseThrow();
        Category clothing = categoryRepository.findBySlug("clothing").orElseThrow();
        Category home = categoryRepository.findBySlug("home").orElseThrow();
        Category sports = categoryRepository.findBySlug("sports").orElseThrow();
 
        List<Product> products = Arrays.asList(
            // Electronics
            createProduct(
                "Wireless Bluetooth Headphones",
                "Premium noise-cancelling headphones with 30-hour battery life. Features advanced ANC technology, comfortable memory foam ear cushions, and crystal-clear audio quality. Perfect for music lovers and professionals alike.",
                BigDecimal.valueOf(79.99),
                BigDecimal.valueOf(99.99),
                "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=400",
                Arrays.asList(
                    "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=400",
                    "https://images.unsplash.com/photo-1484704849700-f032a568e944?w=400",
                    "https://images.unsplash.com/photo-1524678606370-a47ad25cb82a?w=400"
                ),
                electronics,
                Arrays.asList("Black", "White", "Blue"),
                null,
                4.5,
                128,
                45,
                Arrays.asList(
                    new String[]{"Battery Life", "30 hours"},
                    new String[]{"Driver Size", "40mm"},
                    new String[]{"Bluetooth Version", "5.0"},
                    new String[]{"Weight", "250g"},
                    new String[]{"Noise Cancellation", "Active (ANC)"}
                )
            ),
            createProduct(
                "Smart Watch Pro",
                "Advanced fitness tracking smartwatch with heart rate monitor, GPS, and 7-day battery life. Water resistant up to 50 meters, perfect for swimmers and athletes.",
                BigDecimal.valueOf(199.99),
                BigDecimal.valueOf(249.99),
                "https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=400",
                Arrays.asList(
                    "https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=400",
                    "https://images.unsplash.com/photo-1434493789847-2f02dc6ca35d?w=400"
                ),
                electronics,
                Arrays.asList("Black", "Silver", "Rose Gold"),
                null,
                4.8,
                256,
                32,
                Arrays.asList(
                    new String[]{"Display", "1.4\" AMOLED"},
                    new String[]{"Battery Life", "7 days"},
                    new String[]{"Water Resistance", "50m"},
                    new String[]{"GPS", "Built-in"},
                    new String[]{"Compatibility", "iOS & Android"}
                )
            ),
            createProduct(
                "Portable Bluetooth Speaker",
                "Powerful 20W speaker with deep bass, IPX7 waterproof rating, and 12-hour playtime. Perfect for outdoor adventures and beach parties.",
                BigDecimal.valueOf(49.99),
                null,
                "https://images.unsplash.com/photo-1608043152269-423dbba4e7e1?w=400",
                Arrays.asList(
                    "https://images.unsplash.com/photo-1608043152269-423dbba4e7e1?w=400",
                    "https://images.unsplash.com/photo-1589003077984-894e133dabab?w=400"
                ),
                electronics,
                Arrays.asList("Black", "Blue", "Red", "Green"),
                null,
                4.3,
                89,
                78,
                Arrays.asList(
                    new String[]{"Output Power", "20W"},
                    new String[]{"Battery Life", "12 hours"},
                    new String[]{"Water Resistance", "IPX7"},
                    new String[]{"Bluetooth Range", "30ft"},
                    new String[]{"Weight", "540g"}
                )
            ),
            createProduct(
                "4K Webcam",
                "Ultra HD webcam with auto-focus, built-in ring light, and noise-cancelling microphone. Perfect for streaming, video calls, and content creation.",
                BigDecimal.valueOf(89.99),
                BigDecimal.valueOf(119.99),
                "https://images.unsplash.com/photo-1587826080692-f439cd0b70da?w=400",
                Arrays.asList(
                    "https://images.unsplash.com/photo-1587826080692-f439cd0b70da?w=400"
                ),
                electronics,
                Arrays.asList("Black"),
                null,
                4.6,
                167,
                23,
                Arrays.asList(
                    new String[]{"Resolution", "4K @ 30fps"},
                    new String[]{"Field of View", "90°"},
                    new String[]{"Microphone", "Dual stereo"},
                    new String[]{"Auto Focus", "Yes"},
                    new String[]{"Connection", "USB-C"}
                )
            ),
 
            // Clothing
            createProduct(
                "Classic Denim Jacket",
                "Timeless denim jacket with vintage wash finish. Features button closure, chest pockets, and adjustable waist tabs. A wardrobe essential for any season.",
                BigDecimal.valueOf(59.99),
                BigDecimal.valueOf(79.99),
                "https://images.unsplash.com/photo-1551028719-00167b16eac5?w=400",
                Arrays.asList(
                    "https://images.unsplash.com/photo-1551028719-00167b16eac5?w=400",
                    "https://images.unsplash.com/photo-1523205771623-e0faa4d2813d?w=400"
                ),
                clothing,
                Arrays.asList("Light Blue", "Dark Blue", "Black"),
                Arrays.asList("XS", "S", "M", "L", "XL", "XXL"),
                4.7,
                312,
                56,
                Arrays.asList(
                    new String[]{"Material", "100% Cotton Denim"},
                    new String[]{"Closure", "Button"},
                    new String[]{"Pockets", "4"},
                    new String[]{"Care", "Machine washable"},
                    new String[]{"Fit", "Regular"}
                )
            ),
            createProduct(
                "Premium Cotton T-Shirt",
                "Super soft organic cotton t-shirt with a modern fit. Pre-shrunk fabric maintains shape wash after wash. Available in multiple colors.",
                BigDecimal.valueOf(24.99),
                null,
                "https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?w=400",
                Arrays.asList(
                    "https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?w=400",
                    "https://images.unsplash.com/photo-1583743814966-8936f5b7be1a?w=400"
                ),
                clothing,
                Arrays.asList("White", "Black", "Navy", "Gray", "Olive"),
                Arrays.asList("XS", "S", "M", "L", "XL"),
                4.4,
                523,
                150,
                Arrays.asList(
                    new String[]{"Material", "100% Organic Cotton"},
                    new String[]{"Weight", "180 GSM"},
                    new String[]{"Fit", "Modern/Slim"},
                    new String[]{"Care", "Machine washable"},
                    new String[]{"Neck", "Crew neck"}
                )
            ),
            createProduct(
                "Running Sneakers",
                "Lightweight running shoes with responsive cushioning and breathable mesh upper. Designed for both casual wear and athletic performance.",
                BigDecimal.valueOf(89.99),
                BigDecimal.valueOf(119.99),
                "https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=400",
                Arrays.asList(
                    "https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=400",
                    "https://images.unsplash.com/photo-1606107557195-0e29a4b5b4aa?w=400"
                ),
                clothing,
                Arrays.asList("Red", "Black", "White", "Blue"),
                Arrays.asList("7", "8", "9", "10", "11", "12"),
                4.6,
                445,
                38,
                Arrays.asList(
                    new String[]{"Upper", "Breathable mesh"},
                    new String[]{"Sole", "Rubber"},
                    new String[]{"Cushioning", "EVA foam"},
                    new String[]{"Weight", "280g"},
                    new String[]{"Drop", "8mm"}
                )
            ),
 
            // Home & Garden
            createProduct(
                "Minimalist Desk Lamp",
                "Modern LED desk lamp with adjustable brightness and color temperature. Touch control, USB charging port, and memory function.",
                BigDecimal.valueOf(34.99),
                BigDecimal.valueOf(44.99),
                "https://images.unsplash.com/photo-1507473885765-e6ed057f782c?w=400",
                Arrays.asList(
                    "https://images.unsplash.com/photo-1507473885765-e6ed057f782c?w=400",
                    "https://images.unsplash.com/photo-1513506003901-1e6a229e2d15?w=400"
                ),
                home,
                Arrays.asList("White", "Black", "Wood"),
                null,
                4.5,
                198,
                67,
                Arrays.asList(
                    new String[]{"Light Source", "LED"},
                    new String[]{"Brightness Levels", "5"},
                    new String[]{"Color Temperature", "3000K-6000K"},
                    new String[]{"USB Port", "Yes"},
                    new String[]{"Power", "12W"}
                )
            ),
            createProduct(
                "Ceramic Plant Pot Set",
                "Set of 3 modern ceramic planters with drainage holes and bamboo saucers. Perfect for succulents, herbs, or small houseplants.",
                BigDecimal.valueOf(29.99),
                null,
                "https://images.unsplash.com/photo-1485955900006-10f4d324d411?w=400",
                Arrays.asList(
                    "https://images.unsplash.com/photo-1485955900006-10f4d324d411?w=400",
                    "https://images.unsplash.com/photo-1459411552884-841db9b3cc2a?w=400"
                ),
                home,
                Arrays.asList("White", "Terracotta", "Gray"),
                null,
                4.2,
                156,
                89,
                Arrays.asList(
                    new String[]{"Material", "Ceramic"},
                    new String[]{"Set Includes", "3 pots + 3 saucers"},
                    new String[]{"Sizes", "4\", 5\", 6\" diameter"},
                    new String[]{"Drainage", "Yes"},
                    new String[]{"Saucer Material", "Bamboo"}
                )
            ),
            createProduct(
                "Cozy Throw Blanket",
                "Ultra-soft microfiber throw blanket perfect for movie nights and cozy evenings. Machine washable and fade-resistant.",
                BigDecimal.valueOf(39.99),
                BigDecimal.valueOf(54.99),
                "https://images.unsplash.com/photo-1555041469-a586c61ea9bc?w=400",
                Arrays.asList(
                    "https://images.unsplash.com/photo-1555041469-a586c61ea9bc?w=400"
                ),
                home,
                Arrays.asList("Cream", "Gray", "Navy", "Blush"),
                null,
                4.8,
                287,
                0,
                Arrays.asList(
                    new String[]{"Material", "Microfiber fleece"},
                    new String[]{"Size", "50\" x 60\""},
                    new String[]{"Weight", "GSM 280"},
                    new String[]{"Care", "Machine washable"},
                    new String[]{"Features", "Fade-resistant"}
                )
            ),
 
            // Sports
            createProduct(
                "Yoga Mat Premium",
                "Extra thick 6mm yoga mat with non-slip surface and alignment lines. Includes carrying strap. Eco-friendly TPE material.",
                BigDecimal.valueOf(44.99),
                null,
                "https://images.unsplash.com/photo-1601925260368-ae2f83cf8b7f?w=400",
                Arrays.asList(
                    "https://images.unsplash.com/photo-1601925260368-ae2f83cf8b7f?w=400",
                    "https://images.unsplash.com/photo-1544367567-0f2fcb009e0b?w=400"
                ),
                sports,
                Arrays.asList("Purple", "Blue", "Green", "Black"),
                null,
                4.7,
                234,
                112,
                Arrays.asList(
                    new String[]{"Material", "TPE (eco-friendly)"},
                    new String[]{"Thickness", "6mm"},
                    new String[]{"Dimensions", "72\" x 24\""},
                    new String[]{"Features", "Alignment lines"},
                    new String[]{"Includes", "Carrying strap"}
                )
            ),
            createProduct(
                "Adjustable Dumbbells",
                "Space-saving adjustable dumbbells from 5-52.5 lbs per hand. Quick-change weight system perfect for home gyms.",
                BigDecimal.valueOf(299.99),
                BigDecimal.valueOf(349.99),
                "https://images.unsplash.com/photo-1586401100295-7a8096fd231a?w=400",
                Arrays.asList(
                    "https://images.unsplash.com/photo-1586401100295-7a8096fd231a?w=400",
                    "https://images.unsplash.com/photo-1583454110551-21f2fa2afe61?w=400"
                ),
                sports,
                Arrays.asList("Black/Red"),
                null,
                4.9,
                178,
                8,
                Arrays.asList(
                    new String[]{"Weight Range", "5-52.5 lbs"},
                    new String[]{"Increments", "2.5 lb"},
                    new String[]{"Material", "Steel + rubber grip"},
                    new String[]{"Warranty", "2 years"},
                    new String[]{"Storage Tray", "Included"}
                )
            )
        );
 
        productRepository.saveAll(products);
        log.info("Seeded {} products", products.size());
 
        // Add some reviews to products
        seedReviews(products);
    }

    private Product createProduct(
            String name,
            String description,
            BigDecimal price,
            BigDecimal originalPrice,
            String image,
            List<String> images,
            Category category,
            List<String> colors,
            List<String> sizes,
            double rating,
            int reviewCount,
            int stock,
            List<String[]> specs
    ) {
        Product product = new Product();
        product.setName(name);
        product.setDescription(description);
        product.setPrice(price);
        product.setOriginalPrice(originalPrice);
        product.setImage(image);
        product.setImages(images);
        product.setCategory(category);
        product.setColors(colors);
        product.setSizes(sizes);
        product.setRating(rating);
        product.setReviewCount(reviewCount);
        product.setStock(stock);
        product.setFeatured(Math.random() > 0.5);
        product.setActive(true);
 
        // Add specifications
        for (String[] spec : specs) {
            ProductSpecification specification = new ProductSpecification();
            specification.setLabel(spec[0]);
            specification.setValue(spec[1]);
            specification.setProduct(product);
            product.getSpecifications().add(specification);
        }
 
        return product;
    }
 
    private void seedReviews(List<Product> products) {
        String[] reviewerNames = {"John D.", "Sarah M.", "Mike R.", "Emily W.", "Chris L.", "Jessica T.", "David K.", "Amanda P."};
        String[] reviewTitles = {
            "Excellent quality!",
            "Great value for money",
            "Exceeded expectations",
            "Good but could be better",
            "Highly recommend!",
            "Perfect for my needs",
            "Very satisfied",
            "Amazing product"
        };
        String[] reviewComments = {
            "This product is exactly what I was looking for. The quality is outstanding and delivery was fast.",
            "I've been using this for a month now and it's held up great. Would definitely buy again.",
            "The build quality is impressive for the price. Very happy with my purchase.",
            "Good product overall. A few minor issues but nothing major. Customer service was helpful.",
            "Absolutely love it! It arrived quickly and works perfectly. Five stars!",
            "This has become my go-to. Great quality and exactly as described.",
            "Solid product that does what it promises. No complaints here.",
            "Better than expected! The attention to detail is remarkable."
        };
 
        int reviewIndex = 0;
        for (Product product : products) {
            // Add 2-4 reviews per product
            int numReviews = 2 + (int)(Math.random() * 3);
            for (int i = 0; i < numReviews; i++) {
                Review review = new Review();
                review.setProduct(product);
                review.setReviewerName(reviewerNames[reviewIndex % reviewerNames.length]);
                review.setRating(4 + (int)(Math.random() * 2)); // 4-5 stars
                review.setTitle(reviewTitles[reviewIndex % reviewTitles.length]);
                review.setComment(reviewComments[reviewIndex % reviewComments.length]);
                review.setHelpful((int)(Math.random() * 50));
                review.setVerified(Math.random() > 0.3);
                review.setCreatedAt(LocalDateTime.now().minusDays((long)(Math.random() * 90)));
                reviewRepository.save(review);
                reviewIndex++;
            }
        }
        log.info("Seeded reviews for products");
    }
 
    private void seedCoupons() {
        List<Coupon> coupons = Arrays.asList(
            createCoupon("SAVE10", Coupon.DiscountType.PERCENT, BigDecimal.valueOf(10), BigDecimal.valueOf(50), 100),
            createCoupon("FLAT20", Coupon.DiscountType.FIXED, BigDecimal.valueOf(20), BigDecimal.valueOf(100), 50),
            createCoupon("WELCOME15", Coupon.DiscountType.PERCENT, BigDecimal.valueOf(15), BigDecimal.valueOf(0), 500),
            createCoupon("SUMMER25", Coupon.DiscountType.PERCENT, BigDecimal.valueOf(25), BigDecimal.valueOf(75), 200)
        );
        couponRepository.saveAll(coupons);
        log.info("Seeded {} coupons", coupons.size());
    }
 
    private Coupon createCoupon(String code, Coupon.DiscountType type, BigDecimal value, BigDecimal minPurchase, int usageLimit) {
        Coupon coupon = new Coupon();
        coupon.setCode(code);
        coupon.setType(type);
        coupon.setDiscount(value);
        coupon.setMinOrder(minPurchase);
        coupon.setUsageLimit(usageLimit);
        coupon.setUsageCount(0);
        coupon.setActive(true);
        coupon.setValidUntil(LocalDateTime.now().plusMonths(3));
        return coupon;
    }
 
    private void seedTestUser() {
        // Create a test user for development
        User testUser = new User();
        testUser.setEmail("test@randomstuff.shop");
        testUser.setPassword(passwordEncoder.encode("password123"));
        testUser.setName("Test User");
        testUser.setRole(User.Role.USER);
        userRepository.save(testUser);
 
        // Create an admin user
        User adminUser = new User();
        adminUser.setEmail("admin@randomstuff.shop");
        adminUser.setPassword(passwordEncoder.encode("admin123"));
        adminUser.setName("Admin User");
        adminUser.setRole(User.Role.ADMIN);
        userRepository.save(adminUser);
 
        log.info("Seeded test users");
    }
}