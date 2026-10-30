package com.phlbd.dto.payment;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RazorpayOrderRequest {
    @NotBlank(message = "Order ID is required")
    private String orderId;
}
