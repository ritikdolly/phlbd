package com.phlbd.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;

@Document(collection = "food_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FoodItem extends BaseDocument {

    @Id
    private String id;

    private String name;
    private String description;
    private BigDecimal price;
    private BigDecimal originalPrice;

    @Indexed
    private String categoryId;
    private String categoryName;

    @Indexed
    private String categorySlug;

    private String imageUrl;

    @Builder.Default
    private BigDecimal rating = BigDecimal.valueOf(5.0);

    @Builder.Default
    private Integer reviewCount = 0;

    @Indexed
    @Builder.Default
    private boolean isVeg = true;

    @Builder.Default
    private boolean isSpicy = false;

    @Builder.Default
    private boolean isChefSpecial = false;

    @Builder.Default
    private boolean isBestseller = false;

    private String cookTime;
    private Integer calories;

    @Indexed
    @Builder.Default
    private boolean available = true;

    private String tags;
    private String customizationsJson;
}
