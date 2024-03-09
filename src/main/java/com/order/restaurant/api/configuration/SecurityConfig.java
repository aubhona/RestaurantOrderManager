package com.order.restaurant.api.configuration;

import com.order.restaurant.api.service.security.AuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class SecurityConfig {
    private static String cookieAuthName;
    private static String tokenKey;
    private static String[] publicEndpoints;

    @Autowired
    public SecurityConfig(Environment environment) {
        cookieAuthName = environment.getProperty("app.cookie-auth-name");
        tokenKey = environment.getProperty("app.token-key");
        publicEndpoints = environment.getProperty("app.public-endpoints", String[].class);
    }

    @Bean
    public FilterRegistrationBean<AuthenticationFilter> authenticationFilter() {
        FilterRegistrationBean<AuthenticationFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new AuthenticationFilter(cookieAuthName, tokenKey, publicEndpoints));
        registrationBean.addUrlPatterns("/*");
        return registrationBean;
    }
}
