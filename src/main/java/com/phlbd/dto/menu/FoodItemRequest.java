package com.phlbd.dto.menu;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FoodItemRequest {
    @NotBlank(message = "Item name is required")
    private String name;

    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than zero")
    private BigDecimal price;

    private BigDecimal originalPrice;

    @NotBlank(message = "Category ID is required")
    private String categoryId;

    private String imageUrl;
    private Boolean isVeg;
    private Boolean isSpicy;
    private Boolean isChefSpecial;
    private Boolean isBestseller;
    private String cookTime;
    private Integer calories;
    private Boolean available;
    private String tags;
    private String customizationsJson;
}
