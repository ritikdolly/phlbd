package com.phlbd.dto.menu;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDto {
    private String id;
    private String name;
    private String slug;
    private String description;
    private String icon;
    private Integer displayOrder;
    private boolean active;
    private int itemCount;
}
