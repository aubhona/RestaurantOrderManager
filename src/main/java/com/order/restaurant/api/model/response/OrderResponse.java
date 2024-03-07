package com.order.restaurant.api.model.response;

import com.order.restaurant.api.model.Dish;
import com.order.restaurant.api.model.Feedback;
import com.order.restaurant.api.model.Status;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class OrderResponse {
    private Long Id;
    private LocalDateTime createdDateTime;
    private Status status;
    private List<OrderDishResponse> dishes;
    private FeedbackResponse feedback;
}
