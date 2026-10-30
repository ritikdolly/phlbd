package com.phlbd.dto.order;

import com.phlbd.entity.enums.OrderStatus;
import com.phlbd.entity.enums.PaymentMethod;
import com.phlbd.entity.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDto {
    private String id;
    private String orderNumber;
    private String customerId;
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private String deliveryAddressSnapshot;
    private OrderStatus status;
    private BigDecimal subtotal;
    private BigDecimal taxAmount;
    private BigDecimal deliveryFee;
    private BigDecimal discountAmount;
    private BigDecimal totalAmount;
    private PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus;
    private String couponCode;
    private String deliveryInstructions;
    private String cancellationReason;
    private LocalDateTime createdAt;
    private List<OrderItemDto> items;
    private String assignedDeliveryManId;
    private String assignedDeliveryManName;
}
