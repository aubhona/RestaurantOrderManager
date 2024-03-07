package com.order.restaurant.api.controller;

import com.order.restaurant.api.model.response.StatsResponse;
import com.order.restaurant.api.service.StatisticService;
import com.order.restaurant.api.service.security.handler.TokenHandler;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.expression.AccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/stats")
public class StatisticController {
    private final String tokenKey;
    private final String cookieAuthName;
    private final StatisticService statisticService;

    @Autowired
    public StatisticController(StatisticService statisticService, Environment environment) {
        this.statisticService = statisticService;
        this.cookieAuthName = environment.getProperty("app.cookie-auth-name");
        this.tokenKey = environment.getProperty("app.token-key");
    }

    @GetMapping
    public ResponseEntity<StatsResponse> getStats(HttpServletRequest request) throws AccessException {
        String username = TokenHandler.getUsernameFromToken(
                TokenHandler.getTokenFromCookies(request, cookieAuthName),
                tokenKey
        );

        if (!statisticService.isAdmin(username)) {
            throw new AccessException("Permission denied.");
        }

        StatsResponse statsResponse = new StatsResponse();
        statsResponse.setTotalRevenue(statisticService.getTotalRevenue());
        statsResponse.setDishStatistics(statisticService.getDishStats());
        statsResponse.setAdminsCount(statisticService.getAdminCount());
        statsResponse.setUserCount(statisticService.getUserCount());

        return ResponseEntity.ok(statsResponse);
    }


}
