package com.phlbd.entity;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Address {

    @Builder.Default
    private String id = UUID.randomUUID().toString();

    private String label; // "Home", "Work", "Other"
    private String streetLine;
    private String city;
    private String state;
    private String postalCode;
    private String landmark;
    private String contactPhone;

    @Builder.Default
    private boolean isDefault = false;
}
