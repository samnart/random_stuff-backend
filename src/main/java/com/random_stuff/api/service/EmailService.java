package com.random_stuff.api.service;

import com.random_stuff.api.entity.Order;
import com.random_stuff.api.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.math.BigDecimal;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${app.mail.from:noreply@randomstuff.shop}")
    private String fromEmail;

    @Value("${app.mail.enabled:false}")
    private boolean emailEnabled;

    @Value("${spring.application.name:RandomStuff}")
    private String appName;

    // ===== ORDER EMAILS =====

    @Async
    public void sendOrderConfirmation(Order order) {
        if (!emailEnabled) {
            log.info("Email disabled. Would send order confirmation for order: {}", order.getId());
            return;
        }

        Context context = new Context();
        context.setVariable("order", order);
        context.setVariable("orderNumber", order.getId().substring(0, 8).toUpperCase());
        context.setVariable("customerName", order.getUser().getName());
        context.setVariable("items", order.getItems());
        context.setVariable("subtotal", order.getSubtotal());
        context.setVariable("shipping", order.getShippingCost());
        context.setVariable("discount", order.getDiscount());
        context.setVariable("total", order.getTotal());
        context.setVariable("shippingAddress", order.getShippingAddress());

        String subject = "Order Confirmation - #" + order.getId().substring(0, 8).toUpperCase();
        sendHtmlEmail(order.getUser().getEmail(), subject, "order-confirmation", context);
    }

    @Async
    public void sendOrderStatusUpdate(Order order) {
        if (!emailEnabled) {
            log.info("Email disabled. Would send status update for order: {}", order.getId());
            return;
        }

        Context context = new Context();
        context.setVariable("orderNumber", order.getId().substring(0, 8).toUpperCase());
        context.setVariable("customerName", order.getUser().getName());
        context.setVariable("status", formatStatus(order.getStatus().name()));
        context.setVariable("statusMessage", getStatusMessage(order.getStatus()));

        String subject = "Order Update - #" + order.getId().substring(0, 8).toUpperCase();
        sendHtmlEmail(order.getUser().getEmail(), subject, "order-status", context);
    }

    @Async
    public void sendShippingNotification(Order order, String trackingNumber) {
        if (!emailEnabled) {
            log.info("Email disabled. Would send shipping notification for order: {}", order.getId());
            return;
        }

        Context context = new Context();
        context.setVariable("orderNumber", order.getId().substring(0, 8).toUpperCase());
        context.setVariable("customerName", order.getUser().getName());
        context.setVariable("trackingNumber", trackingNumber);
        context.setVariable("shippingAddress", order.getShippingAddress());

        String subject = "Your Order Has Shipped! - #" + order.getId().substring(0, 8).toUpperCase();
        sendHtmlEmail(order.getUser().getEmail(), subject, "shipping-notification", context);
    }

    // ===== AUTH EMAILS =====

    @Async
    public void sendWelcomeEmail(User user) {
        if (!emailEnabled) {
            log.info("Email disabled. Would send welcome email to: {}", user.getEmail());
            return;
        }

        Context context = new Context();
        context.setVariable("name", user.getName());
        context.setVariable("appName", appName);

        sendHtmlEmail(user.getEmail(), "Welcome to " + appName + "!", "welcome", context);
    }

    @Async
    public void sendPasswordResetEmail(String email, String resetToken, String resetUrl) {
        if (!emailEnabled) {
            log.info("Email disabled. Password reset token for {}: {}", email, resetToken);
            return;
        }

        Context context = new Context();
        context.setVariable("resetUrl", resetUrl + "?token=" + resetToken);
        context.setVariable("token", resetToken);
        context.setVariable("expiryHours", 1);

        sendHtmlEmail(email, "Password Reset Request", "password-reset", context);
    }

    @Async
    public void sendPasswordChangedConfirmation(User user) {
        if (!emailEnabled) {
            log.info("Email disabled. Would send password changed confirmation to: {}", user.getEmail());
            return;
        }

        Context context = new Context();
        context.setVariable("name", user.getName());

        sendHtmlEmail(user.getEmail(), "Password Changed Successfully", "password-changed", context);
    }

    // ===== PROMOTIONAL EMAILS =====

    @Async
    public void sendPromotionalEmail(String email, String subject, String promoCode,
                                     BigDecimal discountPercent, String expiryDate) {
        if (!emailEnabled) {
            log.info("Email disabled. Would send promo email to: {}", email);
            return;
        }

        Context context = new Context();
        context.setVariable("promoCode", promoCode);
        context.setVariable("discount", discountPercent);
        context.setVariable("expiryDate", expiryDate);

        sendHtmlEmail(email, subject, "promotional", context);
    }

    @Async
    public void sendAbandonedCartReminder(User user, Map<String, Object> cartData) {
        if (!emailEnabled) {
            log.info("Email disabled. Would send abandoned cart reminder to: {}", user.getEmail());
            return;
        }

        Context context = new Context();
        context.setVariable("name", user.getName());
        context.setVariable("cartItems", cartData.get("items"));
        context.setVariable("cartTotal", cartData.get("total"));

        sendHtmlEmail(user.getEmail(), "You left something behind!", "abandoned-cart", context);
    }

    // ===== ADMIN ALERTS =====

    @Async
    public void sendLowStockAlert(String adminEmail, String productName, int currentStock) {
        if (!emailEnabled) {
            log.info("Email disabled. Low stock alert for: {} ({})", productName, currentStock);
            return;
        }

        Context context = new Context();
        context.setVariable("productName", productName);
        context.setVariable("currentStock", currentStock);

        sendHtmlEmail(adminEmail, "Low Stock Alert: " + productName, "low-stock-alert", context);
    }

    @Async
    public void sendDailySummary(String adminEmail, Map<String, Object> summaryData) {
        if (!emailEnabled) {
            log.info("Email disabled. Would send daily summary to: {}", adminEmail);
            return;
        }

        Context context = new Context();
        context.setVariables(summaryData);

        sendHtmlEmail(adminEmail, "Daily Summary - " + appName, "daily-summary", context);
    }

    // ===== CORE EMAIL SENDING =====

    private void sendHtmlEmail(String to, String subject, String template, Context context) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);

            String htmlContent = templateEngine.process("emails/" + template, context);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Email sent successfully to: {} (template: {})", to, template);

        } catch (MessagingException e) {
            log.error("Failed to send email to: {} - {}", to, e.getMessage());
        }
    }

    // ===== SIMPLE TEXT EMAIL (fallback) =====

    public void sendSimpleEmail(String to, String subject, String body) {
        if (!emailEnabled) {
            log.info("Email disabled. Would send to {}: {} - {}", to, subject, body);
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, false);

            mailSender.send(message);
            log.info("Simple email sent to: {}", to);

        } catch (MessagingException e) {
            log.error("Failed to send simple email to: {} - {}", to, e.getMessage());
        }
    }

    // ===== HELPER METHODS =====

    private String formatStatus(String status) {
        return status.substring(0, 1).toUpperCase() +
               status.substring(1).toLowerCase().replace("_", " ");
    }

    private String getStatusMessage(Order.OrderStatus status) {
        return switch (status) {
            case PROCESSING -> "We've received your order and are preparing it for shipment.";
            case CONFIRMED -> "Your order has been confirmed and will be shipped soon.";
            case SHIPPED -> "Great news! Your order is on its way to you.";
            case DELIVERED -> "Your order has been delivered. We hope you love it!";
            case CANCELLED -> "Your order has been cancelled. If you have questions, please contact us.";
            case REFUNDED -> "Your refund has been processed. Please allow 5-10 business days for it to appear.";
        };
    }
}
