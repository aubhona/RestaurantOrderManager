package com.order.restaurant.api.controller;

import com.order.restaurant.api.model.Dish;
import com.order.restaurant.api.model.request.DishRequest;
import com.order.restaurant.api.model.request.DishUpdateRequest;
import com.order.restaurant.api.model.request.ListRequest;
import com.order.restaurant.api.model.response.ApiResponse;
import com.order.restaurant.api.model.response.DishesApiResponse;
import com.order.restaurant.api.model.response.ListDishesResponse;
import com.order.restaurant.api.service.MenuService;
import com.order.restaurant.api.service.security.handler.TokenHandler;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.expression.AccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("api/menu/dishes")
public class MenuController {
    private final String tokenKey;
    private final String cookieAuthName;
    private final MenuService menuService;

    @Autowired
    public MenuController(MenuService menuService, Environment environment) {
        this.menuService = menuService;
        this.cookieAuthName = environment.getProperty("app.cookie-auth-name");
        this.tokenKey = environment.getProperty("app.token-key");
    }

    @GetMapping
    public ResponseEntity<ListDishesResponse> getMenu() {
        List<Dish> dishes = menuService.getAllDishes();

        return ResponseEntity.ok(new ListDishesResponse(dishes, (long) dishes.size()));
    }

    @PostMapping
    public ResponseEntity<DishesApiResponse> addDish(
            HttpServletRequest request,
            @RequestBody
            @Validated
            ListRequest<DishRequest> dishRequests
    ) throws AccessException {
        String username = TokenHandler.getUsernameFromToken(
                TokenHandler.getTokenFromCookies(request, cookieAuthName),
                tokenKey
        );

        if (!menuService.isAdmin(username)) {
            throw new AccessException("Permission denied.");
        }

        List<String> messages = new ArrayList<>();
        List<Dish> dishes = menuService.addAllDishes(dishRequests.getRequests().stream()
                .filter(dishRequest -> {
                    if (menuService.existByName(dishRequest.name())) {
                        messages.add(
                                String.format("Dish with with the name = %s already exists. " +
                                        "It will not be added to the menu.", dishRequest.name()));
                        return false;
                    }

                    return true;
                })
                .map(dishRequest -> {
                    Dish dish = new Dish();
                    dish.setName(dishRequest.name());
                    dish.setPrice(dishRequest.price());
                    dish.setCookingDuration(dishRequest.cookingDuration());
                    dish.setCount(dishRequest.count());

                    return dish;
                })
                .toList());

        DishesApiResponse apiResponse = new DishesApiResponse();

        if (!messages.isEmpty()) {
            apiResponse.setDescription(
                    String.join(" ", messages)
                            + " The rest of the dishes have been added successfully."
            );
        } else {
            apiResponse.setDescription("The dishes have been added successfully.");
        }

        apiResponse.setDishes(dishes);

        return ResponseEntity.ok(apiResponse);
    }

    @PutMapping
    public ResponseEntity<ApiResponse> changeDish(
            HttpServletRequest request,
            @Valid
            @RequestBody
            ListRequest<DishUpdateRequest> dishRequests
    ) throws AccessException {
        String username = TokenHandler.getUsernameFromToken(
                TokenHandler.getTokenFromCookies(request, cookieAuthName),
                tokenKey
        );

        if (!menuService.isAdmin(username)) {
            throw new AccessException("Permission denied.");
        }

        List<String> messages = new ArrayList<>();

        menuService.updateDishes(dishRequests.getRequests().stream()
                .filter(dishRequest -> {
                    if (!menuService.existById(dishRequest.id())) {
                        messages.add(
                                String.format("Dish with with the name = %s doesn't exist. " +
                                        "It will be ignored.", dishRequest.name()));

                        return false;
                    }

                    return true;
                })
                .map(dishRequest -> {
                    Dish dish = new Dish();
                    dish.setName(dishRequest.name());
                    dish.setPrice(dishRequest.price());
                    dish.setCookingDuration(dishRequest.cookingDuration());
                    dish.setId(dishRequest.id());
                    dish.setCount(dishRequest.count());

                    return dish;
                })
                .toList()
        );

        ApiResponse apiResponse = new ApiResponse();

        if (!messages.isEmpty()) {
            apiResponse.setDescription(
                    String.join(" ", messages)
                            + " The rest of the dishes have been changed successfully."
            );
        } else {
            apiResponse.setDescription("The dishes have been changed successfully.");
        }


        return ResponseEntity.ok(apiResponse);
    }
}
