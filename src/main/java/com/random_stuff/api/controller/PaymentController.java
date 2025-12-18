package com.random_stuff.api.controller;

import com.random_stuff.api.dto.ApiResponse;
import com.random_stuff.api.service.PaymentService;
import com.random_stuff.api.service.PaymentService.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/checkout-session")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<CheckoutSessionResult>> createCheckoutSession(
            @Valid @RequestBody CreateCheckoutRequest request) {
        CheckoutSessionResult result = paymentService.createCheckoutSession(
            request.getOrderId(),
            request.getSuccessUrl(),
            request.getCancelUrl()
        );
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PostMapping("/payment-intent")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PaymentIntentResult>> createPaymentIntent(
            @Valid @RequestBody CreatePaymentIntentRequest request) {
        PaymentIntentResult result = paymentService.createPaymentIntent(request.getOrderId());
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String signature) {
        paymentService.handleWebhook(payload, signature);
        return ResponseEntity.ok("Webhook received");
    }

    @PostMapping("/confirm-manual/{orderId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> confirmManually(@PathVariable String orderId) {
        paymentService.confirmPaymentManually(orderId);
        return ResponseEntity.ok(ApiResponse.success("Payment confirmed manually", null));
    }

    @PostMapping("/refund")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<RefundResult>> refund(
            @Valid @RequestBody RefundRequest request) {
        RefundResult result = paymentService.refundPayment(
            request.getOrderId(),
            request.getAmount(),
            request.getReason()
        );
        return ResponseEntity.ok(ApiResponse.success("Refund processed", result));
    }

    // ===== REQUEST DTOs =====

    @Data
    public static class CreateCheckoutRequest {
        @NotBlank(message = "Order ID is required")
        private String orderId;

        @NotBlank(message = "Success URL is required")
        private String successUrl;

        @NotBlank(message = "Cancel URL is required")
        private String cancelUrl;
    }

    @Data
    public static class CreatePaymentIntentRequest {
        @NotBlank(message = "Order ID is required")
        private String orderId;
    }

    @Data
    public static class RefundRequest {
        @NotBlank(message = "Order ID is required")
        private String orderId;

        private BigDecimal amount; // null = full refund

        private String reason;
    }
}
