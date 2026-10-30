package com.phlbd.dto.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemDto {
    private String id;
    private String foodItemId;
    private String itemName;
    private BigDecimal itemPrice;
    private Integer quantity;
    private BigDecimal subtotal;
    private String selectedCustomizations;
}
