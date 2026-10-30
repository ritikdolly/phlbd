package com.phlbd.dto.auth;

import com.phlbd.entity.enums.RoleType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private String id;
    private String fullName;
    private String email;
    private String phone;
    private RoleType role;
    private String avatarUrl;
    private boolean active;
}
