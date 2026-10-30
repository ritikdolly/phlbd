package com.phlbd.entity.enums;

import java.util.EnumSet;
import java.util.Set;

public enum OrderStatus {
    PENDING,
    CONFIRMED,
    PREPARING,
    READY_FOR_PICKUP,
    PICKED_UP,
    OUT_FOR_DELIVERY,
    DELIVERED,
    CANCELLED,
    REJECTED,
    FAILED_DELIVERY;

    public boolean canTransitionTo(OrderStatus next) {
        if (this == next) return true;
        return switch (this) {
            case PENDING -> EnumSet.of(CONFIRMED, REJECTED, CANCELLED).contains(next);
            case CONFIRMED -> EnumSet.of(PREPARING, CANCELLED).contains(next);
            case PREPARING -> EnumSet.of(READY_FOR_PICKUP, CANCELLED).contains(next);
            case READY_FOR_PICKUP -> EnumSet.of(PICKED_UP, CANCELLED).contains(next);
            case PICKED_UP -> EnumSet.of(OUT_FOR_DELIVERY, FAILED_DELIVERY).contains(next);
            case OUT_FOR_DELIVERY -> EnumSet.of(DELIVERED, FAILED_DELIVERY).contains(next);
            case DELIVERED, CANCELLED, REJECTED, FAILED_DELIVERY -> false;
        };
    }

    public boolean isTerminal() {
        return this == DELIVERED || this == CANCELLED || this == REJECTED || this == FAILED_DELIVERY;
    }
}

