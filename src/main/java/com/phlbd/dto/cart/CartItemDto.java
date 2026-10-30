package com.phlbd.dto.cart;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItemDto {
    private String id;
    private String foodItemId;
    private String foodName;
    private String foodImage;
    private BigDecimal unitPrice;
    private Integer quantity;
    private BigDecimal subtotal;
    private String selectedCustomizations;
}
