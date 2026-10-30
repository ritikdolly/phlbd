package com.phlbd.dto.customer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressDto {
    private String id;
    private String label;
    private String streetLine;
    private String city;
    private String state;
    private String postalCode;
    private String landmark;
    private String contactPhone;
    private boolean isDefault;
}
