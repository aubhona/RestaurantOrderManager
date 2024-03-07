package com.order.restaurant.api.model.request;

import jakarta.validation.Valid;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ListRequest<T> {
    @Valid
    private List<T> requests;
}
