package com.phlbd.repository;

import com.phlbd.entity.Category;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends MongoRepository<Category, String> {
    Optional<Category> findBySlug(String slug);
    List<Category> findByActiveTrueOrderByDisplayOrderAsc();
    boolean existsBySlug(String slug);
}
