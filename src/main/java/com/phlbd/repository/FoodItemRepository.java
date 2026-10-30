package com.phlbd.repository;

import com.phlbd.entity.FoodItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FoodItemRepository extends MongoRepository<FoodItem, String> {

    List<FoodItem> findByAvailableTrue();

    List<FoodItem> findByCategoryIdAndAvailableTrue(String categoryId);

    List<FoodItem> findByCategorySlugAndAvailableTrue(String categorySlug);

    @Query("{ 'available': true, '$or': [ " +
           "{ 'name': { '$regex': ?0, '$options': 'i' } }, " +
           "{ 'description': { '$regex': ?0, '$options': 'i' } }, " +
           "{ 'tags': { '$regex': ?0, '$options': 'i' } } " +
           "] }")
    List<FoodItem> searchMenu(String query);

    Page<FoodItem> findByAvailable(boolean available, Pageable pageable);

    long countByAvailableTrue();

    long countByCategoryId(String categoryId);
}
