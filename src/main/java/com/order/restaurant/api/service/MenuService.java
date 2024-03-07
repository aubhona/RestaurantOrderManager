package com.order.restaurant.api.service;

import com.order.restaurant.api.model.Dish;
import com.order.restaurant.api.model.Role;
import com.order.restaurant.api.model.User;
import com.order.restaurant.api.service.repository.DishRepository;
import com.order.restaurant.api.service.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class MenuService {
    private final UserRepository userRepository;
    private final DishRepository dishRepository;

    @Autowired
    public MenuService(UserRepository userRepository, DishRepository dishRepository) {
        this.userRepository = userRepository;
        this.dishRepository = dishRepository;
    }

    @Transactional
    public boolean isAdmin(String username) {
        User user = userRepository.findUserByUsername(username);
        return user != null && user.getRole() == Role.ADMIN;
    }

    @Transactional
    public List<Dish> getAllDishes() {
        return dishRepository.findAll();
    }

    @Transactional
    public boolean existByName(String... dishNames) {
        return Arrays.stream(dishNames).allMatch(dishName -> dishRepository.findDishByName(dishName).isPresent());
    }

    @Transactional
    public boolean existById(Long... dishIds) {
        return Arrays.stream(dishIds).allMatch(dishId -> dishRepository.findById(dishId).isPresent());
    }


    @Transactional
    public List<Dish> addAllDishes(List<Dish> dishes) {
        dishRepository.saveAll(dishes);
        return dishes;
    }

    @Transactional
    public void updateDishes(List<Dish> newDishes) {
        dishRepository.saveAll(newDishes);
    }
}
