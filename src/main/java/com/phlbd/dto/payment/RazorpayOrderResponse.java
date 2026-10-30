package com.phlbd.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RazorpayOrderResponse {
    private String orderId;
    private String orderNumber;
    private String razorpayOrderId;
    private BigDecimal amount;
    private String currency;
    private String razorpayKeyId;
}
