package com.phlbd.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "reviews")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review extends BaseDocument {

    @Id
    private String id;

    @Indexed
    private String foodItemId;
    private String foodItemName;

    @Indexed
    private String customerId;
    private String customerName;
    private String customerAvatar;

    @Indexed
    private String orderId;

    private Integer rating;
    private String comment;
}
