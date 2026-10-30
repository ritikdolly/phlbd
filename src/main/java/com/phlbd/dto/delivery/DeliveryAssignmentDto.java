package com.phlbd.dto.delivery;

import com.phlbd.entity.enums.DeliveryStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryAssignmentDto {
    private String id;
    private String orderId;
    private String orderNumber;
    private String deliveryManId;
    private String deliveryManName;
    private String deliveryManPhone;
    private LocalDateTime assignedAt;
    private LocalDateTime pickedUpAt;
    private LocalDateTime deliveredAt;
    private DeliveryStatus status;
    private String failureReason;
    private String deliveryNotes;
    private String deliveryAddress;
    private String customerName;
    private String customerPhone;
    private Object orderSummary;
}
