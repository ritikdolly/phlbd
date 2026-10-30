package com.phlbd.dto.owner;

import com.phlbd.entity.enums.RoleType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StaffManagementRequest {
    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Valid email is required")
    private String email;

    private String password; // Optional when updating
    private String phone;

    @NotNull(message = "Role is required (ROLE_STAFF or ROLE_DELIVERY_MAN)")
    private RoleType role;

    private Boolean active;
}

