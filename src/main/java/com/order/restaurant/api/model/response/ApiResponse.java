package com.order.restaurant.api.model.response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ApiResponse {
    private String description;
    private String code;
    private String exceptionName;
    private String exceptionMessage;
}
