package com.order.restaurant.api.service.repository;

import com.order.restaurant.api.model.Revenue;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
@Hidden
public interface RevenueRepository extends JpaRepository<Revenue, Long> {

}
