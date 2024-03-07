package com.order.restaurant.api.model.request;

import jakarta.validation.constraints.Positive;

public record OrderRequest(
        @Positive(message = "Id must be positive")
        Long dishId,
        @Positive(message = "Count must be positive")
        Long count
) {
}
