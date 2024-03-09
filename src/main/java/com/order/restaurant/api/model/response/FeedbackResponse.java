package com.order.restaurant.api.model.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FeedbackResponse {
    private String text;
    private Integer rating;
}
