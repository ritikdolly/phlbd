package com.phlbd.dto.payment;

import com.phlbd.entity.enums.PaymentMethod;
import com.phlbd.entity.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDto {
    private String id;
    private String orderId;
    private String transactionId;
    private PaymentMethod paymentMethod;
    private String razorpayOrderId;
    private String razorpayPaymentId;
    private BigDecimal amount;
    private String currency;
    private PaymentStatus status;
    private LocalDateTime paymentDate;
    private String failureReason;
}
