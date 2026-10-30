package com.phlbd.dto.delivery;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignDeliveryRequest {
    @NotBlank(message = "Order ID is required")
    private String orderId;

    @NotBlank(message = "Delivery personnel user ID is required")
    private String deliveryManId;

    private String notes;
}
