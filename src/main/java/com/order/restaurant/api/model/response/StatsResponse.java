package com.order.restaurant.api.model.response;

import com.order.restaurant.api.model.DishStatistics;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Setter
@Getter
public class StatsResponse {
    List<DishStatistics> dishStatistics;
    BigDecimal totalRevenue;
    Long userCount;
    Long adminsCount;
}
