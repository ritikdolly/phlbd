package com.phlbd.entity;

import com.phlbd.entity.enums.RoleType;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document(collection = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseDocument {

    @Id
    private String id;

    private String fullName;

    @Indexed(unique = true)
    private String email;

    private String passwordHash;

    private String phone;

    @Indexed
    private RoleType role;

    private String avatarUrl;

    @Builder.Default
    private boolean active = true;

    @Builder.Default
    private List<Address> addresses = new ArrayList<>();

    public void addAddress(Address address) {
        addresses.add(address);
    }

    public void removeAddress(String addressId) {
        addresses.removeIf(a -> a.getId().equals(addressId));
    }
}
