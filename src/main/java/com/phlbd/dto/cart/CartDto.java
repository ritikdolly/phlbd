package com.phlbd.dto.cart;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartDto {
    private String id;
    private List<CartItemDto> items;
    private int totalQuantity;
    private BigDecimal subtotal;
}
