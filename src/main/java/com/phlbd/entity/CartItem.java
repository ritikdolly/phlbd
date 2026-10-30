package com.phlbd.entity;

import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItem {

    @Builder.Default
    private String id = UUID.randomUUID().toString();

    private String foodItemId;
    private String foodName;
    private String foodImage;
    private Integer quantity;
    private BigDecimal unitPrice;
    private String selectedCustomizations;
}
