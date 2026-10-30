package com.phlbd.entity;

import com.phlbd.entity.enums.PaymentMethod;
import com.phlbd.entity.enums.PaymentStatus;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Document(collection = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment extends BaseDocument {

    @Id
    private String id;

    @Indexed
    private String orderId;

    private String orderNumber;
    private String transactionId;
    private PaymentMethod paymentMethod;

    @Indexed
    private String razorpayOrderId;

    @Indexed
    private String razorpayPaymentId;

    private String razorpaySignature;
    private BigDecimal amount;

    @Builder.Default
    private String currency = "INR";

    private PaymentStatus status;
    private LocalDateTime paymentDate;
    private String failureReason;
}
