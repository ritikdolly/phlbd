package com.phlbd.service;

import com.phlbd.common.EntityMapper;
import com.phlbd.common.PagedResponse;
import com.phlbd.dto.delivery.DeliveryAssignmentDto;
import com.phlbd.dto.delivery.DeliveryStatusUpdateRequest;
import com.phlbd.entity.DeliveryAssignment;
import com.phlbd.entity.Order;
import com.phlbd.entity.enums.DeliveryStatus;
import com.phlbd.entity.enums.OrderStatus;
import com.phlbd.entity.enums.PaymentMethod;
import com.phlbd.entity.enums.PaymentStatus;
import com.phlbd.exception.BadRequestException;
import com.phlbd.exception.ForbiddenException;
import com.phlbd.exception.ResourceNotFoundException;
import com.phlbd.repository.DeliveryAssignmentRepository;
import com.phlbd.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DeliveryService {

    private final DeliveryAssignmentRepository deliveryAssignmentRepository;
    private final OrderRepository orderRepository;

    public List<DeliveryAssignmentDto> getActiveAssignments(String deliveryManId) {
        return deliveryAssignmentRepository.findByDeliveryManIdOrderByAssignedAtDesc(deliveryManId)
                .stream()
                .filter(da -> da.getStatus() != DeliveryStatus.DELIVERED && da.getStatus() != DeliveryStatus.FAILED)
                .map(EntityMapper::toDeliveryAssignmentDto)
                .collect(Collectors.toList());
    }

    public PagedResponse<DeliveryAssignmentDto> getDeliveryHistory(String deliveryManId, Pageable pageable) {
        Page<DeliveryAssignment> page = deliveryAssignmentRepository
                .findByDeliveryManIdOrderByAssignedAtDesc(deliveryManId, pageable);
        return PagedResponse.of(page.map(EntityMapper::toDeliveryAssignmentDto));
    }

    public DeliveryAssignmentDto updateDeliveryStatus(String assignmentId, String deliveryManId, DeliveryStatusUpdateRequest request) {
        DeliveryAssignment assignment = deliveryAssignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("DeliveryAssignment", "id", assignmentId));

        if (!assignment.getDeliveryManId().equals(deliveryManId)) {
            throw new ForbiddenException("Cannot update delivery assignment belonging to another driver");
        }

        DeliveryStatus newStatus = request.getStatus();
        Order order = orderRepository.findById(assignment.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", assignment.getOrderId()));

        LocalDateTime now = LocalDateTime.now();

        switch (newStatus) {
            case PICKED_UP -> {
                assignment.setPickedUpAt(now);
                assignment.setStatus(DeliveryStatus.PICKED_UP);
                order.setStatus(OrderStatus.PICKED_UP);
            }
            case OUT_FOR_DELIVERY -> {
                assignment.setStatus(DeliveryStatus.OUT_FOR_DELIVERY);
                order.setStatus(OrderStatus.OUT_FOR_DELIVERY);
            }
            case DELIVERED -> {
                assignment.setDeliveredAt(now);
                assignment.setStatus(DeliveryStatus.DELIVERED);
                order.setStatus(OrderStatus.DELIVERED);

                if (order.getPaymentMethod() == PaymentMethod.CASH_ON_DELIVERY) {
                    order.setPaymentStatus(PaymentStatus.COMPLETED);
                    if (order.getPayment() != null) {
                        order.getPayment().setStatus(PaymentStatus.COMPLETED);
                        order.getPayment().setPaymentDate(now);
                    }
                }
            }
            case FAILED -> {
                if (request.getFailureReason() == null || request.getFailureReason().isBlank()) {
                    throw new BadRequestException("Failure reason is mandatory when reporting a failed delivery attempt");
                }
                assignment.setStatus(DeliveryStatus.FAILED);
                assignment.setFailureReason(request.getFailureReason());
                order.setStatus(OrderStatus.FAILED_DELIVERY);
            }
            default -> throw new BadRequestException("Unsupported delivery status: " + newStatus);
        }

        if (request.getNotes() != null) {
            assignment.setDeliveryNotes(request.getNotes());
        }

        order.setDeliveryAssignment(assignment);
        orderRepository.save(order);
        DeliveryAssignment saved = deliveryAssignmentRepository.save(assignment);
        return EntityMapper.toDeliveryAssignmentDto(saved);
    }
}
