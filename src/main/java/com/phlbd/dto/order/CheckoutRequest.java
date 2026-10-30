package com.phlbd.dto.order;

import com.phlbd.dto.customer.AddressRequest;
import com.phlbd.entity.enums.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutRequest {
    private String addressId;
    private AddressRequest newAddress;

    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;

    private String couponCode;
    private String deliveryInstructions;
}
