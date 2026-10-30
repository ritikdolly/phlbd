package com.phlbd.repository;

import com.phlbd.entity.DeliveryAssignment;
import com.phlbd.entity.enums.DeliveryStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeliveryAssignmentRepository extends MongoRepository<DeliveryAssignment, String> {
    Optional<DeliveryAssignment> findByOrderId(String orderId);
    List<DeliveryAssignment> findByDeliveryManIdOrderByAssignedAtDesc(String deliveryManId);
    Page<DeliveryAssignment> findByDeliveryManIdOrderByAssignedAtDesc(String deliveryManId, Pageable pageable);
    List<DeliveryAssignment> findByDeliveryManIdAndStatus(String deliveryManId, DeliveryStatus status);
    long countByDeliveryManIdAndStatus(String deliveryManId, DeliveryStatus status);
    List<DeliveryAssignment> findByStatus(DeliveryStatus status);
}
