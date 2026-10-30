package com.phlbd.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category extends BaseDocument {

    @Id
    private String id;

    private String name;

    @Indexed(unique = true)
    private String slug;

    private String description;
    private String icon;

    @Builder.Default
    private Integer displayOrder = 0;

    @Builder.Default
    private boolean active = true;
}
