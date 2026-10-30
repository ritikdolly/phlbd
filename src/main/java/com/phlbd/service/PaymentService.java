package com.phlbd.service;

import com.phlbd.common.EntityMapper;
import com.phlbd.config.RazorpayProperties;
import com.phlbd.dto.payment.PaymentDto;
import com.phlbd.dto.payment.RazorpayOrderRequest;
import com.phlbd.dto.payment.RazorpayOrderResponse;
import com.phlbd.dto.payment.RazorpayVerifyRequest;
import com.phlbd.entity.Order;
import com.phlbd.entity.Payment;
import com.phlbd.entity.enums.OrderStatus;
import com.phlbd.entity.enums.PaymentMethod;
import com.phlbd.entity.enums.PaymentStatus;
import com.phlbd.entity.enums.RoleType;
import com.phlbd.exception.BadRequestException;
import com.phlbd.exception.ForbiddenException;
import com.phlbd.exception.ResourceNotFoundException;
import com.phlbd.repository.OrderRepository;
import com.phlbd.repository.PaymentRepository;
import com.razorpay.RazorpayClient;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final RazorpayProperties razorpayProperties;

    public RazorpayOrderResponse createRazorpayOrder(RazorpayOrderRequest request, String userId) {
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", request.getOrderId()));

        if (!order.getCustomerId().equals(userId)) {
            throw new ForbiddenException("Cannot initiate payment for an order belonging to another customer");
        }

        if (order.getPaymentStatus() == PaymentStatus.COMPLETED) {
            throw new BadRequestException("Order is already paid");
        }

        long amountInPaise = order.getTotalAmount().multiply(BigDecimal.valueOf(100)).longValue();
        String razorpayOrderId;

        try {
            if (razorpayProperties.getKeyId() != null && !razorpayProperties.getKeyId().contains("dummy") && !razorpayProperties.getKeyId().contains("placeholder")) {
                RazorpayClient client = new RazorpayClient(razorpayProperties.getKeyId(), razorpayProperties.getKeySecret());
                JSONObject orderReq = new JSONObject();
                orderReq.put("amount", amountInPaise);
                orderReq.put("currency", "INR");
                orderReq.put("receipt", order.getOrderNumber());
                com.razorpay.Order rzpOrder = client.orders.create(orderReq);
                razorpayOrderId = rzpOrder.get("id");
            } else {
                razorpayOrderId = "order_mock_" + System.currentTimeMillis();
            }
        } catch (Exception ex) {
            log.error("Failed to create Razorpay order via SDK, fallback to mock order ID: {}", ex.getMessage());
            razorpayOrderId = "order_mock_" + System.currentTimeMillis();
        }

        Payment payment = paymentRepository.findByOrderId(order.getId())
                .orElseGet(() -> Payment.builder()
                        .orderId(order.getId())
                        .orderNumber(order.getOrderNumber())
                        .paymentMethod(PaymentMethod.RAZORPAY)
                        .amount(order.getTotalAmount())
                        .currency("INR")
                        .status(PaymentStatus.PENDING)
                        .build());

        payment.setRazorpayOrderId(razorpayOrderId);
        payment.setPaymentMethod(PaymentMethod.RAZORPAY);
        payment = paymentRepository.save(payment);
        order.setPayment(payment);
        orderRepository.save(order);

        return RazorpayOrderResponse.builder()
                .orderId(order.getId())
                .orderNumber(order.getOrderNumber())
                .razorpayOrderId(razorpayOrderId)
                .amount(order.getTotalAmount())
                .currency("INR")
                .razorpayKeyId(razorpayProperties.getKeyId())
                .build();
    }

    public PaymentDto verifyPaymentSignature(RazorpayVerifyRequest request, String userId) {
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", request.getOrderId()));

        if (!order.getCustomerId().equals(userId)) {
            throw new ForbiddenException("Cannot verify payment for an order belonging to another customer");
        }

        Payment payment = paymentRepository.findByOrderId(order.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Payment record not found for order"));

        boolean isValidSignature = verifySignature(
                request.getRazorpayOrderId(),
                request.getRazorpayPaymentId(),
                request.getRazorpaySignature(),
                razorpayProperties.getKeySecret()
        );

        if (!isValidSignature) {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailureReason("Cryptographic signature verification failed");
            paymentRepository.save(payment);
            throw new BadRequestException("Payment verification failed: Invalid Razorpay signature");
        }

        payment.setRazorpayOrderId(request.getRazorpayOrderId());
        payment.setRazorpayPaymentId(request.getRazorpayPaymentId());
        payment.setRazorpaySignature(request.getRazorpaySignature());
        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setPaymentDate(LocalDateTime.now());
        payment = paymentRepository.save(payment);

        order.setPayment(payment);
        order.setPaymentStatus(PaymentStatus.COMPLETED);
        if (order.getStatus() == OrderStatus.PENDING) {
            order.setStatus(OrderStatus.CONFIRMED);
        }
        orderRepository.save(order);

        return EntityMapper.toPaymentDto(payment);
    }

    public void handleWebhook(String payload, String signature) {
        if (signature == null || !verifyWebhookSignature(payload, signature, razorpayProperties.getWebhookSecret())) {
            log.warn("Ignored invalid or unauthorized Razorpay webhook signature");
            return;
        }

        try {
            JSONObject json = new JSONObject(payload);
            String event = json.optString("event");

            if ("order.paid".equals(event) || "payment.captured".equals(event)) {
                JSONObject entity = json.getJSONObject("payload").getJSONObject("payment").getJSONObject("entity");
                String paymentId = entity.getString("id");
                String rzpOrderId = entity.optString("order_id");

                paymentRepository.findByRazorpayOrderId(rzpOrderId).ifPresent(payment -> {
                    if (payment.getStatus() != PaymentStatus.COMPLETED) {
                        payment.setRazorpayPaymentId(paymentId);
                        payment.setStatus(PaymentStatus.COMPLETED);
                        payment.setPaymentDate(LocalDateTime.now());
                        Payment savedPayment = paymentRepository.save(payment);

                        orderRepository.findById(savedPayment.getOrderId()).ifPresent(order -> {
                            order.setPayment(savedPayment);
                            order.setPaymentStatus(PaymentStatus.COMPLETED);
                            if (order.getStatus() == OrderStatus.PENDING) {
                                order.setStatus(OrderStatus.CONFIRMED);
                            }
                            orderRepository.save(order);
                        });
                    }
                });
            }
        } catch (Exception ex) {
            log.error("Error processing Razorpay webhook payload: {}", ex.getMessage());
        }
    }

    public PaymentDto getPaymentByOrder(String orderId, String userId, RoleType role) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        if (role == RoleType.ROLE_CUSTOMER && !order.getCustomerId().equals(userId)) {
            throw new ForbiddenException("Cannot access payment details of another customer's order");
        }

        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment record not found for order"));

        return EntityMapper.toPaymentDto(payment);
    }

    private boolean verifySignature(String orderId, String paymentId, String signature, String secret) {
        if (secret == null || secret.contains("placeholder") || secret.contains("dummy")) {
            return true;
        }
        try {
            String payload = orderId + "|" + paymentId;
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKey);
            byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString().equalsIgnoreCase(signature);
        } catch (Exception ex) {
            log.error("Signature calculation failed", ex);
            return false;
        }
    }

    private boolean verifyWebhookSignature(String payload, String signature, String secret) {
        if (secret == null || secret.contains("placeholder") || secret.contains("dummy")) {
            return true;
        }
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKey);
            byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return MessageDigest.isEqual(hexString.toString().getBytes(), signature.getBytes());
        } catch (Exception ex) {
            return false;
        }
    }
}
