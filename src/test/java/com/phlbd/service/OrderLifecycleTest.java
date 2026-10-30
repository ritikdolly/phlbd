package com.phlbd.service;

import com.phlbd.entity.enums.OrderStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OrderLifecycleTest {

    @Test
    @DisplayName("Valid Order Status Transitions follow strict state machine")
    void testValidTransitions() {
        // PENDING -> CONFIRMED, REJECTED, CANCELLED
        assertTrue(OrderStatus.PENDING.canTransitionTo(OrderStatus.CONFIRMED));
        assertTrue(OrderStatus.PENDING.canTransitionTo(OrderStatus.REJECTED));
        assertTrue(OrderStatus.PENDING.canTransitionTo(OrderStatus.CANCELLED));

        // CONFIRMED -> PREPARING, CANCELLED
        assertTrue(OrderStatus.CONFIRMED.canTransitionTo(OrderStatus.PREPARING));
        assertTrue(OrderStatus.CONFIRMED.canTransitionTo(OrderStatus.CANCELLED));

        // PREPARING -> READY_FOR_PICKUP, CANCELLED
        assertTrue(OrderStatus.PREPARING.canTransitionTo(OrderStatus.READY_FOR_PICKUP));
        assertTrue(OrderStatus.PREPARING.canTransitionTo(OrderStatus.CANCELLED));

        // READY_FOR_PICKUP -> PICKED_UP
        assertTrue(OrderStatus.READY_FOR_PICKUP.canTransitionTo(OrderStatus.PICKED_UP));

        // PICKED_UP -> OUT_FOR_DELIVERY, FAILED_DELIVERY
        assertTrue(OrderStatus.PICKED_UP.canTransitionTo(OrderStatus.OUT_FOR_DELIVERY));
        assertTrue(OrderStatus.PICKED_UP.canTransitionTo(OrderStatus.FAILED_DELIVERY));

        // OUT_FOR_DELIVERY -> DELIVERED, FAILED_DELIVERY
        assertTrue(OrderStatus.OUT_FOR_DELIVERY.canTransitionTo(OrderStatus.DELIVERED));
        assertTrue(OrderStatus.OUT_FOR_DELIVERY.canTransitionTo(OrderStatus.FAILED_DELIVERY));
    }

    @Test
    @DisplayName("Invalid Order Status Transitions are strictly rejected")
    void testInvalidTransitions() {
        // Cannot jump directly from PENDING to DELIVERED
        assertFalse(OrderStatus.PENDING.canTransitionTo(OrderStatus.DELIVERED));
        // Cannot jump directly from PENDING to OUT_FOR_DELIVERY
        assertFalse(OrderStatus.PENDING.canTransitionTo(OrderStatus.OUT_FOR_DELIVERY));
        // Terminal states cannot transition to anything
        assertFalse(OrderStatus.DELIVERED.canTransitionTo(OrderStatus.PREPARING));
        assertFalse(OrderStatus.CANCELLED.canTransitionTo(OrderStatus.CONFIRMED));
        assertFalse(OrderStatus.REJECTED.canTransitionTo(OrderStatus.PENDING));
        assertFalse(OrderStatus.FAILED_DELIVERY.canTransitionTo(OrderStatus.DELIVERED));
    }

    @Test
    @DisplayName("Terminal states correctly identified")
    void testTerminalStates() {
        assertTrue(OrderStatus.DELIVERED.isTerminal());
        assertTrue(OrderStatus.CANCELLED.isTerminal());
        assertTrue(OrderStatus.REJECTED.isTerminal());
        assertTrue(OrderStatus.FAILED_DELIVERY.isTerminal());

        assertFalse(OrderStatus.PENDING.isTerminal());
        assertFalse(OrderStatus.CONFIRMED.isTerminal());
        assertFalse(OrderStatus.PREPARING.isTerminal());
        assertFalse(OrderStatus.OUT_FOR_DELIVERY.isTerminal());
    }
}

