package com.phlbd.repository;

import com.phlbd.entity.User;
import com.phlbd.entity.enums.RoleType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    List<User> findByRole(RoleType role);
    Page<User> findByRole(RoleType role, Pageable pageable);
    long countByRole(RoleType role);
}
