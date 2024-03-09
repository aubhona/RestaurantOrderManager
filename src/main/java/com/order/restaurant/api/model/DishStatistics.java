package com.order.restaurant.api.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Setter
@Getter
@AllArgsConstructor
public class DishStatistics {
    private String dishName;
    private Long ordersCount;
    private Double averageRating;
}
