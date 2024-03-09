package com.order.restaurant.api.model.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class FeedbackRequest {
    @Size(max = 500)
    private String text;
    @Min(1)
    @Max(5)
    private Integer rating;
}
