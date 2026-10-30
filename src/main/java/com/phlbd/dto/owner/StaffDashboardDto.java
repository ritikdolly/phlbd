package com.phlbd.dto.owner;

import com.phlbd.dto.order.OrderDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StaffDashboardDto {
    private long incomingOrdersCount;
    private long preparingOrdersCount;
    private long readyForPickupCount;
    private List<OrderDto> incomingOrders;
    private List<OrderDto> preparingOrders;
    private List<OrderDto> readyOrders;
}

