package com.random_stuff.api.service;

import com.random_stuff.api.dto.NotificationDto;
import com.random_stuff.api.entity.Order;
import com.random_stuff.api.entity.Product;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    // ===== ADMIN NOTIFICATIONS (broadcast to /topic/admin) =====

    public void notifyNewOrder(Order order) {
        NotificationDto notification = NotificationDto.orderNotification(
            order.getId(),
            order.getUser().getName(),
            "$" + order.getTotal().toString(),
            NotificationDto.NotificationType.NEW_ORDER
        );
        notification.setData(order.getId());

        sendToAdmins(notification);
        log.info("Sent new order notification: {}", order.getId());
    }

    public void notifyOrderStatusChanged(Order order) {
        NotificationDto notification = NotificationDto.builder()
            .id(java.util.UUID.randomUUID().toString())
            .type(NotificationDto.NotificationType.ORDER_STATUS_CHANGED.name())
            .title("Order Status Updated")
            .message(String.format("Order #%s is now %s",
                order.getId().substring(0, 8),
                order.getStatus().name().toLowerCase()))
            .link("/admin/orders/" + order.getId())
            .read(false)
            .createdAt(java.time.LocalDateTime.now())
            .build();

        sendToAdmins(notification);

        // Also notify the customer
        sendToUser(order.getUser().getId(), notification);
    }

    public void notifyOrderPaid(Order order) {
        NotificationDto notification = NotificationDto.orderNotification(
            order.getId(),
            order.getUser().getName(),
            "$" + order.getTotal().toString(),
            NotificationDto.NotificationType.ORDER_PAID
        );

        sendToAdmins(notification);
    }

    public void notifyLowStock(Product product) {
        NotificationDto notification = NotificationDto.stockNotification(
            product.getId(),
            product.getName(),
            product.getStock(),
            NotificationDto.NotificationType.LOW_STOCK
        );

        sendToAdmins(notification);
        log.warn("Low stock alert: {} has {} items left", product.getName(), product.getStock());
    }

    public void notifyOutOfStock(Product product) {
        NotificationDto notification = NotificationDto.stockNotification(
            product.getId(),
            product.getName(),
            0,
            NotificationDto.NotificationType.OUT_OF_STOCK
        );

        sendToAdmins(notification);
        log.error("Out of stock: {}", product.getName());
    }

    public void notifyNewReview(String productName, String userName, int rating) {
        NotificationDto notification = NotificationDto.reviewNotification(productName, userName, rating);
        sendToAdmins(notification);
    }

    // ===== USER NOTIFICATIONS =====

    public void sendToUser(String userId, NotificationDto notification) {
        messagingTemplate.convertAndSendToUser(
            userId,
            "/queue/notifications",
            notification
        );
        log.debug("Sent notification to user {}: {}", userId, notification.getTitle());
    }

    public void notifyUserOrderUpdate(String userId, Order order) {
        NotificationDto notification = NotificationDto.builder()
            .id(java.util.UUID.randomUUID().toString())
            .type(NotificationDto.NotificationType.ORDER_STATUS_CHANGED.name())
            .title("Order Update")
            .message(String.format("Your order #%s is now %s",
                order.getId().substring(0, 8),
                formatStatus(order.getStatus())))
            .link("/orders/" + order.getId())
            .read(false)
            .createdAt(java.time.LocalDateTime.now())
            .build();

        sendToUser(userId, notification);
    }

    // ===== BROADCAST NOTIFICATIONS =====

    public void broadcastFlashSale(String title, String message, String link) {
        NotificationDto notification = NotificationDto.flashSaleNotification(title, message, link);
        messagingTemplate.convertAndSend("/topic/promotions", notification);
        log.info("Broadcast flash sale: {}", title);
    }

    public void broadcastPromotion(String title, String message, String couponCode) {
        NotificationDto notification = NotificationDto.builder()
            .id(java.util.UUID.randomUUID().toString())
            .type(NotificationDto.NotificationType.PROMOTION.name())
            .title(title)
            .message(message)
            .data(couponCode)
            .read(false)
            .createdAt(java.time.LocalDateTime.now())
            .build();

        messagingTemplate.convertAndSend("/topic/promotions", notification);
    }

    // ===== HELPER METHODS =====

    private void sendToAdmins(NotificationDto notification) {
        messagingTemplate.convertAndSend("/topic/admin", notification);
    }

    private String formatStatus(Order.OrderStatus status) {
        return switch (status) {
            case PROCESSING -> "being processed";
            case CONFIRMED -> "confirmed";
            case SHIPPED -> "shipped";
            case DELIVERED -> "delivered";
            case CANCELLED -> "cancelled";
            case REFUNDED -> "refunded";
        };
    }
}
