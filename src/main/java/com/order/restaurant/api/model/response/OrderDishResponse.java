package com.order.restaurant.api.model.response;

public record OrderDishResponse(Long quantity, String dishName, Long price) {
}
