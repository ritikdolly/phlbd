package com.phlbd.dto.coupon;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplyCouponRequest {
    @NotBlank(message = "Coupon code is required")
    private String code;

    @NotNull(message = "Order subtotal is required")
    private BigDecimal subtotal;
}

