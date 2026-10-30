package com.phlbd.dto.owner;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantSettingsRequest {
    private String name;
    private String tagline;
    private String description;
    private String phone;
    private String email;
    private String address;
    private String operatingHours;
    private BigDecimal deliveryFee;
    private BigDecimal minOrderAmount;
    private BigDecimal taxPercentage;
    private Boolean active;
}

