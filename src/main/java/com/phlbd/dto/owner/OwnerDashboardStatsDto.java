package com.phlbd.dto.owner;

import com.phlbd.dto.order.OrderDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OwnerDashboardStatsDto {
    private BigDecimal totalRevenue;
    private BigDecimal todayRevenue;
    private BigDecimal weekRevenue;
    private BigDecimal monthRevenue;
    private long totalOrders;
    private long todayOrders;
    private long pendingOrders;
    private long preparingOrders;
    private long readyOrders;
    private long outForDeliveryOrders;
    private long deliveredOrders;
    private long cancelledOrders;
    private long totalStaffCount;
    private long activeDeliveryPersonnelCount;
    private long totalCustomersCount;
    private List<OrderDto> recentOrders;
}

