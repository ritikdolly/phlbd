package com.phlbd.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;

@Document(collection = "restaurants")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Restaurant extends BaseDocument {

    @Id
    private String id;

    private String name;
    private String tagline;
    private String description;
    private String phone;
    private String email;
    private String address;
    private String operatingHours;

    @Builder.Default
    private BigDecimal deliveryFee = BigDecimal.valueOf(50.00);

    @Builder.Default
    private BigDecimal minOrderAmount = BigDecimal.valueOf(200.00);

    @Builder.Default
    private BigDecimal taxPercentage = BigDecimal.valueOf(5.00);

    @Builder.Default
    private boolean active = true;
}
