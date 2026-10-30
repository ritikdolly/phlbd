package com.phlbd.service;

import com.phlbd.common.EntityMapper;
import com.phlbd.dto.order.OrderDto;
import com.phlbd.dto.owner.StaffDashboardDto;
import com.phlbd.entity.Order;
import com.phlbd.entity.enums.OrderStatus;
import com.phlbd.exception.InvalidOrderStateException;
import com.phlbd.exception.ResourceNotFoundException;
import com.phlbd.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StaffService {

    private final OrderRepository orderRepository;

    public StaffDashboardDto getStaffDashboard() {
        List<Order> pending = orderRepository.findByStatusOrderByCreatedAtAsc(OrderStatus.PENDING);
        List<Order> confirmedAndPreparing = orderRepository.findByStatusIn(List.of(OrderStatus.CONFIRMED, OrderStatus.PREPARING));
        List<Order> ready = orderRepository.findByStatusOrderByCreatedAtAsc(OrderStatus.READY_FOR_PICKUP);

        return StaffDashboardDto.builder()
                .incomingOrdersCount(pending.size())
                .preparingOrdersCount(confirmedAndPreparing.size())
                .readyForPickupCount(ready.size())
                .incomingOrders(pending.stream().map(EntityMapper::toOrderDto).collect(Collectors.toList()))
                .preparingOrders(confirmedAndPreparing.stream().map(EntityMapper::toOrderDto).collect(Collectors.toList()))
                .readyOrders(ready.stream().map(EntityMapper::toOrderDto).collect(Collectors.toList()))
                .build();
    }

    public List<OrderDto> getIncomingOrders() {
        return orderRepository.findByStatusOrderByCreatedAtAsc(OrderStatus.PENDING)
                .stream()
                .map(EntityMapper::toOrderDto)
                .collect(Collectors.toList());
    }

    public OrderDto acceptOrder(String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new InvalidOrderStateException("Only PENDING orders can be accepted. Current: " + order.getStatus());
        }

        order.setStatus(OrderStatus.CONFIRMED);
        Order saved = orderRepository.save(order);
        return EntityMapper.toOrderDto(saved);
    }

    public OrderDto rejectOrder(String orderId, String reason) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new InvalidOrderStateException("Only PENDING orders can be rejected. Current: " + order.getStatus());
        }

        order.setStatus(OrderStatus.REJECTED);
        order.setCancellationReason(reason != null ? reason : "Rejected by kitchen staff");
        Order saved = orderRepository.save(order);
        return EntityMapper.toOrderDto(saved);
    }

    public OrderDto startPreparing(String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        if (order.getStatus() != OrderStatus.CONFIRMED) {
            throw new InvalidOrderStateException("Order must be CONFIRMED before preparing. Current: " + order.getStatus());
        }

        order.setStatus(OrderStatus.PREPARING);
        Order saved = orderRepository.save(order);
        return EntityMapper.toOrderDto(saved);
    }

    public OrderDto markReadyForPickup(String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        if (order.getStatus() != OrderStatus.PREPARING) {
            throw new InvalidOrderStateException("Order must be PREPARING before marking READY_FOR_PICKUP. Current: " + order.getStatus());
        }

        order.setStatus(OrderStatus.READY_FOR_PICKUP);
        Order saved = orderRepository.save(order);
        return EntityMapper.toOrderDto(saved);
    }
}
