package com.phlbd.entity;

import com.phlbd.entity.enums.OrderStatus;
import com.phlbd.entity.enums.PaymentMethod;
import com.phlbd.entity.enums.PaymentStatus;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order extends BaseDocument {

    @Id
    private String id;

    @Indexed(unique = true)
    private String orderNumber;

    @Indexed
    private String customerId;
    private String customerName;
    private String customerEmail;
    private String customerPhone;

    private String restaurantId;
    private String addressId;
    private String deliveryAddressSnapshot;

    @Indexed
    private OrderStatus status;

    private BigDecimal subtotal;
    private BigDecimal taxAmount;
    private BigDecimal deliveryFee;

    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;

    private BigDecimal totalAmount;

    private PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus;

    private String couponCode;
    private String deliveryInstructions;
    private String cancellationReason;

    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();

    private Payment payment;
    private DeliveryAssignment deliveryAssignment;

    public void addItem(OrderItem item) {
        items.add(item);
    }
}
