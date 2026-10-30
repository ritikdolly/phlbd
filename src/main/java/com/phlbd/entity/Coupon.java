package com.phlbd.entity;

import com.phlbd.entity.enums.DiscountType;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Document(collection = "coupons")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Coupon extends BaseDocument {

    @Id
    private String id;

    @Indexed(unique = true)
    private String code;

    private DiscountType discountType;
    private BigDecimal discountValue;

    @Builder.Default
    private BigDecimal minOrderValue = BigDecimal.ZERO;

    private BigDecimal maxDiscount;
    private LocalDateTime validFrom;
    private LocalDateTime validUntil;

    @Builder.Default
    private Integer usageLimit = 1000;

    @Builder.Default
    private Integer timesUsed = 0;

    @Builder.Default
    private boolean active = true;

    public boolean isValidForAmount(BigDecimal amount) {
        if (!active) return false;
        LocalDateTime now = LocalDateTime.now();
        if (validFrom != null && now.isBefore(validFrom)) return false;
        if (validUntil != null && now.isAfter(validUntil)) return false;
        if (usageLimit != null && timesUsed >= usageLimit) return false;
        if (minOrderValue != null && amount.compareTo(minOrderValue) < 0) return false;
        return true;
    }

    public BigDecimal calculateDiscount(BigDecimal orderSubtotal) {
        if (!isValidForAmount(orderSubtotal)) return BigDecimal.ZERO;
        BigDecimal calculated;
        if (discountType == DiscountType.PERCENTAGE) {
            calculated = orderSubtotal.multiply(discountValue).divide(BigDecimal.valueOf(100));
        } else {
            calculated = discountValue;
        }
        if (maxDiscount != null && calculated.compareTo(maxDiscount) > 0) {
            calculated = maxDiscount;
        }
        return calculated.min(orderSubtotal);
    }
}
