package com.phlbd.dto.order;

import com.phlbd.entity.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusUpdateRequest {
    @NotNull(message = "New order status is required")
    private OrderStatus status;

    private String reason;
}

