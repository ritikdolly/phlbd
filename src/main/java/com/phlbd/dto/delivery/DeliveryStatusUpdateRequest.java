package com.phlbd.dto.delivery;

import com.phlbd.entity.enums.DeliveryStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryStatusUpdateRequest {
    @NotNull(message = "Delivery status is required")
    private DeliveryStatus status;

    private String notes;
    private String failureReason;
}

