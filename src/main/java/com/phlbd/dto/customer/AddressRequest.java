package com.phlbd.dto.customer;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressRequest {
    @NotBlank(message = "Label is required (e.g. Home, Work)")
    private String label;

    @NotBlank(message = "Street address is required")
    private String streetLine;

    @NotBlank(message = "City is required")
    private String city;

    private String state;

    @NotBlank(message = "Postal code is required")
    private String postalCode;

    private String landmark;
    private String contactPhone;
    private Boolean isDefault;
}

