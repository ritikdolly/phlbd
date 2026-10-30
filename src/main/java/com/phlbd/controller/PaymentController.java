package com.phlbd.controller;

import com.phlbd.common.ApiResponse;
import com.phlbd.dto.payment.PaymentDto;
import com.phlbd.dto.payment.RazorpayOrderRequest;
import com.phlbd.dto.payment.RazorpayOrderResponse;
import com.phlbd.dto.payment.RazorpayVerifyRequest;
import com.phlbd.entity.enums.RoleType;
import com.phlbd.security.SecurityUtils;
import com.phlbd.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Tag(name = "Payments & Checkout Gateway", description = "Endpoints for Razorpay integration, signature verification, and webhooks")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/create-razorpay-order")
    @Operation(summary = "Initialize a payment and create a Razorpay order")
    public ResponseEntity<ApiResponse<RazorpayOrderResponse>> createRazorpayOrder(
            @Valid @RequestBody RazorpayOrderRequest request) {
        String userId = SecurityUtils.getCurrentUserId();
        RazorpayOrderResponse response = paymentService.createRazorpayOrder(request, userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/verify")
    @Operation(summary = "Verify Razorpay cryptographic signature and confirm payment")
    public ResponseEntity<ApiResponse<PaymentDto>> verifyPayment(
            @Valid @RequestBody RazorpayVerifyRequest request) {
        String userId = SecurityUtils.getCurrentUserId();
        PaymentDto payment = paymentService.verifyPaymentSignature(request, userId);
        return ResponseEntity.ok(ApiResponse.success("Payment verified successfully", payment));
    }

    @PostMapping("/webhook")
    @Operation(summary = "Razorpay server webhook endpoint for idempotent event processing")
    public ResponseEntity<String> handleRazorpayWebhook(
            @RequestBody String payload,
            @RequestHeader(value = "X-Razorpay-Signature", required = false) String signature) {
        paymentService.handleWebhook(payload, signature);
        return ResponseEntity.ok("OK");
    }

    @GetMapping("/order/{orderId}")
    @Operation(summary = "Get payment transaction details for an order")
    public ResponseEntity<ApiResponse<PaymentDto>> getPaymentByOrderId(@PathVariable String orderId) {
        String userId = SecurityUtils.getCurrentUserId();
        RoleType role = SecurityUtils.getCurrentUserRole();
        PaymentDto payment = paymentService.getPaymentByOrder(orderId, userId, role);
        return ResponseEntity.ok(ApiResponse.success(payment));
    }
}
