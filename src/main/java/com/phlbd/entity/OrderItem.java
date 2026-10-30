package com.phlbd.entity;

import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {

    @Builder.Default
    private String id = UUID.randomUUID().toString();

    private String foodItemId;
    private String itemName;
    private BigDecimal itemPrice;
    private Integer quantity;
    private BigDecimal subtotal;
    private String selectedCustomizations;
}
