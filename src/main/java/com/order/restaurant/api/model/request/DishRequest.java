package com.order.restaurant.api.model.request;

import jakarta.validation.constraints.*;

public record DishRequest(
        @Size(min = 1, max = 500, message = "Dish name length must be at least more than 1 and less than 500 chars")
        String name,
        @PositiveOrZero(message = "Price must be not negative")
        Long price,
        @PositiveOrZero(message = "Cooking duration must be not negative")
        Long cookingDuration,
        @Positive(message = "Count must be positive")
        Long count
) {
}
