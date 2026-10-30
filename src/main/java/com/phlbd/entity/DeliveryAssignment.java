package com.phlbd.entity;

import com.phlbd.entity.enums.DeliveryStatus;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "delivery_assignments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryAssignment extends BaseDocument {

    @Id
    private String id;

    @Indexed
    private String orderId;

    private String orderNumber;

    @Indexed
    private String deliveryManId;
    private String deliveryManName;
    private String deliveryManPhone;

    private String assignedById;
    private String assignedByName;

    private LocalDateTime assignedAt;
    private LocalDateTime pickedUpAt;
    private LocalDateTime deliveredAt;

    @Indexed
    private DeliveryStatus status;

    private String failureReason;
    private String deliveryNotes;
    private String deliveryAddress;
    private String customerName;
    private String customerPhone;
}
