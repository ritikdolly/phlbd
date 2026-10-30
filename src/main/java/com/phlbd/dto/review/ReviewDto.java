package com.phlbd.dto.review;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewDto {
    private String id;
    private String foodItemId;
    private String foodItemName;
    private String customerId;
    private String customerName;
    private String customerAvatar;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
}
