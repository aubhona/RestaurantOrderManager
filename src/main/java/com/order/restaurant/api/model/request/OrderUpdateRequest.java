package com.order.restaurant.api.model.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class OrderUpdateRequest {
    @Valid
    List<OrderRequest> orderRequests;
    @Positive(message = "Order id must be positive")
    Long orderId;
}
