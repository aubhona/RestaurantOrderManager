package com.order.restaurant.api.model.response;

import com.order.restaurant.api.model.Dish;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class DishesApiResponse extends ApiResponse {
    private List<Dish> dishes;
}
