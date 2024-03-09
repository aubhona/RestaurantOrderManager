package com.order.restaurant.api.service.repository;

import com.order.restaurant.api.model.Role;
import com.order.restaurant.api.model.User;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
@Hidden
public interface UserRepository extends JpaRepository<User, Long> {
    User findUserByUsername(String username);
    @Query("SELECT COUNT(u) FROM User u WHERE u.role = ?1")
    long countByRole(Role role);
}
