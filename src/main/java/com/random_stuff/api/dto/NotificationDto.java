package com.random_stuff.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDto {
    private String id;
    private String type;
    private String title;
    private String message;
    private String link;
    private Object data;
    private boolean read;
    private LocalDateTime createdAt;

    public enum NotificationType {
        // Order notifications
        NEW_ORDER,
        ORDER_STATUS_CHANGED,
        ORDER_PAID,
        ORDER_CANCELLED,

        // Inventory notifications
        LOW_STOCK,
        OUT_OF_STOCK,

        // User notifications
        NEW_REVIEW,
        NEW_USER,

        // System notifications
        FLASH_SALE,
        PROMOTION,
        SYSTEM_ALERT
    }

    public static NotificationDto orderNotification(String orderId, String customerName, String total, NotificationType type) {
        String title = switch (type) {
            case NEW_ORDER -> "New Order Received";
            case ORDER_STATUS_CHANGED -> "Order Status Updated";
            case ORDER_PAID -> "Payment Received";
            case ORDER_CANCELLED -> "Order Cancelled";
            default -> "Order Update";
        };

        String message = switch (type) {
            case NEW_ORDER -> String.format("New order #%s from %s for %s", orderId.substring(0, 8), customerName, total);
            case ORDER_PAID -> String.format("Payment received for order #%s", orderId.substring(0, 8));
            case ORDER_CANCELLED -> String.format("Order #%s has been cancelled", orderId.substring(0, 8));
            default -> String.format("Order #%s has been updated", orderId.substring(0, 8));
        };

        return NotificationDto.builder()
            .id(java.util.UUID.randomUUID().toString())
            .type(type.name())
            .title(title)
            .message(message)
            .link("/admin/orders/" + orderId)
            .read(false)
            .createdAt(LocalDateTime.now())
            .build();
    }

    public static NotificationDto stockNotification(String productId, String productName, int stock, NotificationType type) {
        String title = type == NotificationType.OUT_OF_STOCK ? "Out of Stock Alert" : "Low Stock Alert";
        String message = type == NotificationType.OUT_OF_STOCK
            ? String.format("%s is out of stock!", productName)
            : String.format("%s is running low (%d items left)", productName, stock);

        return NotificationDto.builder()
            .id(java.util.UUID.randomUUID().toString())
            .type(type.name())
            .title(title)
            .message(message)
            .link("/admin/products/" + productId)
            .read(false)
            .createdAt(LocalDateTime.now())
            .build();
    }

    public static NotificationDto reviewNotification(String productName, String userName, int rating) {
        return NotificationDto.builder()
            .id(java.util.UUID.randomUUID().toString())
            .type(NotificationType.NEW_REVIEW.name())
            .title("New Review Posted")
            .message(String.format("%s left a %d-star review on %s", userName, rating, productName))
            .read(false)
            .createdAt(LocalDateTime.now())
            .build();
    }

    public static NotificationDto flashSaleNotification(String title, String message, String link) {
        return NotificationDto.builder()
            .id(java.util.UUID.randomUUID().toString())
            .type(NotificationType.FLASH_SALE.name())
            .title(title)
            .message(message)
            .link(link)
            .read(false)
            .createdAt(LocalDateTime.now())
            .build();
    }
}
