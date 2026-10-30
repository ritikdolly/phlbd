package com.phlbd.repository;

import com.phlbd.entity.Role;
import com.phlbd.entity.enums.RoleType;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends MongoRepository<Role, String> {
    Optional<Role> findByName(RoleType name);
}
