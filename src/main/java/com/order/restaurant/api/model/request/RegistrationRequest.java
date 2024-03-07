package com.order.restaurant.api.model.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RegistrationRequest {
    @Size(min = 3, max = 500, message = "Username length must be at least more than 3 and less than 500 chars")
    private String username;
    @Size(min = 3, max = 500, message = "Password length must be at least more than 3 and less than 500 chars")
    private String password;
    @NotNull(message = "Secret key must not be null")
    private String secretKey;
}
