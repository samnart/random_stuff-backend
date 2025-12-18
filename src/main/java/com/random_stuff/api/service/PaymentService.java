package com.random_stuff.api.service;

import com.random_stuff.api.entity.Order;
import com.random_stuff.api.exception.BadRequestException;
import com.random_stuff.api.exception.ResourceNotFoundException;
import com.random_stuff.api.repository.OrderRepository;
import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.*;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.RefundCreateParams;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final OrderRepository orderRepository;
    private final NotificationService notificationService;
    private final EmailService emailService;

    @Value("${stripe.api.key:}")
    private String stripeApiKey;

    @Value("${stripe.webhook.secret:}")
    private String webhookSecret;

    @Value("${stripe.enabled:false}")
    private boolean stripeEnabled;

    private static final String CURRENCY = "usd";

    @PostConstruct
    public void init() {
        if (stripeEnabled && !stripeApiKey.isEmpty()) {
            Stripe.apiKey = stripeApiKey;
            log.info("Stripe initialized successfully");
        } else {
            log.info("Stripe is disabled or not configured");
        }
    }

    // ===== CHECKOUT SESSION (Recommended for web) =====

    public CheckoutSessionResult createCheckoutSession(String orderId, String successUrl, String cancelUrl) {
        if (!stripeEnabled) {
            return createMockCheckoutSession(orderId);
        }

        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        try {
            SessionCreateParams.Builder builder = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(successUrl + "?session_id={CHECKOUT_SESSION_ID}")
                .setCancelUrl(cancelUrl)
                .setClientReferenceId(orderId)
                .setCustomerEmail(order.getUser().getEmail())
                .putMetadata("orderId", orderId);

            // Add line items
            for (var item : order.getItems()) {
                builder.addLineItem(
                    SessionCreateParams.LineItem.builder()
                        .setQuantity((long) item.getQuantity())
                        .setPriceData(
                            SessionCreateParams.LineItem.PriceData.builder()
                                .setCurrency(CURRENCY)
                                .setUnitAmount(item.getPrice().multiply(BigDecimal.valueOf(100)).longValue())
                                .setProductData(
                                    SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                        .setName(item.getProductName())
                                        .build()
                                )
                                .build()
                        )
                        .build()
                );
            }

            // Add shipping if applicable
            if (order.getShippingCost().compareTo(BigDecimal.ZERO) > 0) {
                builder.addLineItem(
                    SessionCreateParams.LineItem.builder()
                        .setQuantity(1L)
                        .setPriceData(
                            SessionCreateParams.LineItem.PriceData.builder()
                                .setCurrency(CURRENCY)
                                .setUnitAmount(order.getShippingCost().multiply(BigDecimal.valueOf(100)).longValue())
                                .setProductData(
                                    SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                        .setName("Shipping")
                                        .build()
                                )
                                .build()
                        )
                        .build()
                );
            }

            Session session = Session.create(builder.build());

            log.info("Created checkout session for order {}: {}", orderId, session.getId());

            return CheckoutSessionResult.builder()
                .sessionId(session.getId())
                .url(session.getUrl())
                .orderId(orderId)
                .build();

        } catch (StripeException e) {
            log.error("Failed to create checkout session: {}", e.getMessage());
            throw new BadRequestException("Failed to create payment session: " + e.getMessage());
        }
    }

    // ===== PAYMENT INTENT (For custom payment forms) =====

    public PaymentIntentResult createPaymentIntent(String orderId) {
        if (!stripeEnabled) {
            return createMockPaymentIntent(orderId);
        }

        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        try {
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(order.getTotal().multiply(BigDecimal.valueOf(100)).longValue())
                .setCurrency(CURRENCY)
                .setDescription("Order #" + orderId.substring(0, 8))
                .putMetadata("orderId", orderId)
                .setReceiptEmail(order.getUser().getEmail())
                .build();

            PaymentIntent intent = PaymentIntent.create(params);

            log.info("Created payment intent for order {}: {}", orderId, intent.getId());

            return PaymentIntentResult.builder()
                .paymentIntentId(intent.getId())
                .clientSecret(intent.getClientSecret())
                .orderId(orderId)
                .amount(order.getTotal())
                .build();

        } catch (StripeException e) {
            log.error("Failed to create payment intent: {}", e.getMessage());
            throw new BadRequestException("Failed to create payment: " + e.getMessage());
        }
    }

    // ===== WEBHOOK HANDLING =====

    @Transactional
    public void handleWebhook(String payload, String signature) {
        if (!stripeEnabled) {
            log.info("Stripe disabled, skipping webhook");
            return;
        }

        Event event;
        try {
            event = Webhook.constructEvent(payload, signature, webhookSecret);
        } catch (SignatureVerificationException e) {
            log.error("Invalid webhook signature");
            throw new BadRequestException("Invalid webhook signature");
        }

        log.info("Received Stripe webhook: {}", event.getType());

        switch (event.getType()) {
            case "checkout.session.completed" -> handleCheckoutComplete(event);
            case "payment_intent.succeeded" -> handlePaymentSuccess(event);
            case "payment_intent.payment_failed" -> handlePaymentFailed(event);
            case "charge.refunded" -> handleRefund(event);
            default -> log.debug("Unhandled event type: {}", event.getType());
        }
    }

    private void handleCheckoutComplete(Event event) {
        Session session = (Session) event.getDataObjectDeserializer()
            .getObject().orElse(null);

        if (session == null) return;

        String orderId = session.getClientReferenceId();
        if (orderId == null) {
            orderId = session.getMetadata().get("orderId");
        }

        if (orderId != null) {
            markOrderPaid(orderId, session.getPaymentIntent());
        }
    }

    private void handlePaymentSuccess(Event event) {
        PaymentIntent intent = (PaymentIntent) event.getDataObjectDeserializer()
            .getObject().orElse(null);

        if (intent == null) return;

        String orderId = intent.getMetadata().get("orderId");
        if (orderId != null) {
            markOrderPaid(orderId, intent.getId());
        }
    }

    private void handlePaymentFailed(Event event) {
        PaymentIntent intent = (PaymentIntent) event.getDataObjectDeserializer()
            .getObject().orElse(null);

        if (intent == null) return;

        String orderId = intent.getMetadata().get("orderId");
        log.warn("Payment failed for order: {}", orderId);
    }

    private void handleRefund(Event event) {
        Charge charge = (Charge) event.getDataObjectDeserializer()
            .getObject().orElse(null);

        if (charge == null) return;

        log.info("Refund processed for charge: {}", charge.getId());
    }

    @Transactional
    public void markOrderPaid(String orderId, String paymentReference) {
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null) {
            log.warn("Order not found for payment: {}", orderId);
            return;
        }

        if (order.isPaid()) {
            log.info("Order {} already marked as paid", orderId);
            return;
        }

        order.setPaid(true);
        order.setPaidAt(LocalDateTime.now());
        order.setStatus(Order.OrderStatus.CONFIRMED);
        orderRepository.save(order);

        log.info("Order {} marked as paid (ref: {})", orderId, paymentReference);

        // Send notifications
        notificationService.notifyOrderPaid(order);
        emailService.sendOrderConfirmation(order);
    }

    // ===== REFUNDS =====

    public RefundResult refundPayment(String orderId, BigDecimal amount, String reason) {
        if (!stripeEnabled) {
            return createMockRefund(orderId, amount);
        }

        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        if (!order.isPaid()) {
            throw new BadRequestException("Cannot refund unpaid order");
        }

        try {
            // In production, store the payment intent ID on the order
            RefundCreateParams.Builder params = RefundCreateParams.builder()
                .setReason(RefundCreateParams.Reason.REQUESTED_BY_CUSTOMER);

            if (amount != null) {
                params.setAmount(amount.multiply(BigDecimal.valueOf(100)).longValue());
            }

            Refund refund = Refund.create(params.build());

            // Update order status
            order.setStatus(Order.OrderStatus.REFUNDED);
            orderRepository.save(order);

            log.info("Refund processed for order {}: {}", orderId, refund.getId());

            return RefundResult.builder()
                .refundId(refund.getId())
                .orderId(orderId)
                .amount(BigDecimal.valueOf(refund.getAmount()).divide(BigDecimal.valueOf(100)))
                .status(refund.getStatus())
                .build();

        } catch (StripeException e) {
            log.error("Failed to process refund: {}", e.getMessage());
            throw new BadRequestException("Failed to process refund: " + e.getMessage());
        }
    }

    // ===== MOCK IMPLEMENTATIONS (for testing without Stripe) =====

    private CheckoutSessionResult createMockCheckoutSession(String orderId) {
        log.info("Creating mock checkout session for order: {}", orderId);
        return CheckoutSessionResult.builder()
            .sessionId("mock_session_" + System.currentTimeMillis())
            .url("/mock-checkout?orderId=" + orderId)
            .orderId(orderId)
            .build();
    }

    private PaymentIntentResult createMockPaymentIntent(String orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        log.info("Creating mock payment intent for order: {}", orderId);
        return PaymentIntentResult.builder()
            .paymentIntentId("mock_pi_" + System.currentTimeMillis())
            .clientSecret("mock_secret_" + System.currentTimeMillis())
            .orderId(orderId)
            .amount(order.getTotal())
            .build();
    }

    private RefundResult createMockRefund(String orderId, BigDecimal amount) {
        log.info("Creating mock refund for order: {}", orderId);
        return RefundResult.builder()
            .refundId("mock_refund_" + System.currentTimeMillis())
            .orderId(orderId)
            .amount(amount)
            .status("succeeded")
            .build();
    }

    // ===== MANUAL PAYMENT CONFIRMATION (for testing) =====

    @Transactional
    public void confirmPaymentManually(String orderId) {
        markOrderPaid(orderId, "manual_" + System.currentTimeMillis());
    }

    // ===== DTOs =====

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CheckoutSessionResult {
        private String sessionId;
        private String url;
        private String orderId;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentIntentResult {
        private String paymentIntentId;
        private String clientSecret;
        private String orderId;
        private BigDecimal amount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RefundResult {
        private String refundId;
        private String orderId;
        private BigDecimal amount;
        private String status;
    }
}
