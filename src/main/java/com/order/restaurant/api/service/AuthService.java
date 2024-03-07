package com.order.restaurant.api.service;

import com.order.restaurant.api.model.Role;
import com.order.restaurant.api.model.User;
import com.order.restaurant.api.service.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UserRepository userRepository;

    @Autowired
    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public boolean registerUser(String username, String password, Role role) {
        if (userRepository.findUserByUsername(username) != null) {
            return false;
        }
        User user = new User();
        user.setRole(role);
        user.setUsername(username);
        user.setPassword(password);
        userRepository.save(user);

        return true;
    }

    public boolean loginUser(String username, String password) {
        User user = userRepository.findUserByUsername(username);
        return user != null
                && user.getPassword().equals(password);
    }
}
