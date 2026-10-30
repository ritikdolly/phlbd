package com.phlbd.dto.menu;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FoodItemDto {
    private String id;
    private String name;
    private String description;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private String categoryId;
    private String categoryName;
    private String categorySlug;
    private String imageUrl;
    private BigDecimal rating;
    private Integer reviewCount;
    private boolean isVeg;
    private boolean isSpicy;
    private boolean isChefSpecial;
    private boolean isBestseller;
    private String cookTime;
    private Integer calories;
    private boolean available;
    private String tags;
    private String customizationsJson;
}
