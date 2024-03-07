package com.order.restaurant.api.service;

import com.order.restaurant.api.model.DishStatistics;
import com.order.restaurant.api.model.Revenue;
import com.order.restaurant.api.model.Role;
import com.order.restaurant.api.model.User;
import com.order.restaurant.api.service.repository.DishRepository;
import com.order.restaurant.api.service.repository.RevenueRepository;
import com.order.restaurant.api.service.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class StatisticService {
    private final UserRepository userRepository;
    private final DishRepository dishRepository;
    private final RevenueRepository revenueRepository;

    @Autowired
    public StatisticService(
            UserRepository userRepository,
            DishRepository dishRepository,
            RevenueRepository revenueRepository
    ) {
        this.userRepository = userRepository;
        this.dishRepository = dishRepository;
        this.revenueRepository = revenueRepository;
    }

    public boolean isAdmin(String username) {
        User user = userRepository.findUserByUsername(username);
        return user != null && user.getRole() == Role.ADMIN;
    }

    public List<DishStatistics> getDishStats() {
        return dishRepository.findDishStatistics();
    }

    public BigDecimal getTotalRevenue() {
        Optional<Revenue> revenue = revenueRepository.findById(1L);
        if (revenue.isEmpty()) {
            revenue = Optional.of(new Revenue());
            revenue.get().setTotalAmount(BigDecimal.valueOf(0));
        }

        return revenue.get().getTotalAmount();
    }

    public long getUserCount() {
        return userRepository.countByRole(Role.USER);
    }

    public long getAdminCount() {
        return userRepository.countByRole(Role.ADMIN);
    }
}
