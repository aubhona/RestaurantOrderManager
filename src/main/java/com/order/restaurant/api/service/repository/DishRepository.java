package com.order.restaurant.api.service.repository;

import com.order.restaurant.api.model.Dish;
import com.order.restaurant.api.model.DishStatistics;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@Hidden
public interface DishRepository extends JpaRepository<Dish, Long> {
    Optional<Dish> findDishByName(String name);
    @Query("SELECT new com.order.restaurant.api.model.DishStatistics(d.name, COUNT(od.dish), AVG(f.rating)) " +
            "FROM Dish d JOIN OrderDish od ON d.id = od.dish.id " +
            "JOIN Order o ON od.order.id = o.id " +
            "LEFT JOIN Feedback f ON o.id = f.order.id " +
            "GROUP BY d.name")
    List<DishStatistics> findDishStatistics();
}
