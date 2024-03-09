package com.order.restaurant.api.model.response;

import com.order.restaurant.api.model.Dish;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ListDishesResponse {
    private List<Dish> dishes;
    private Long size;
}
