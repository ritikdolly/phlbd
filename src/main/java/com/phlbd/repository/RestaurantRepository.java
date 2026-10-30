package com.phlbd.repository;

import com.phlbd.entity.Restaurant;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RestaurantRepository extends MongoRepository<Restaurant, String> {
    Optional<Restaurant> findFirstByOrderByIdAsc();
}
