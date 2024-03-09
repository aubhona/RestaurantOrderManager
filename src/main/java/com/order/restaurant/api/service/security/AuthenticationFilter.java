package com.order.restaurant.api.service.security;

import com.order.restaurant.api.service.security.handler.TokenHandler;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.util.Arrays;
import java.util.regex.Pattern;

@RequiredArgsConstructor
public class AuthenticationFilter implements Filter {
    private final String cookieAuthName;
    private final String tokenKey;
    private final String[] publicEndpoints;

    private boolean isPublicEndpoint(String path) {
        for (String patternString : publicEndpoints) {
            Pattern pattern = Pattern.compile(patternString);
            if (pattern.matcher(path).matches()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void doFilter(
            ServletRequest servletRequest,
            ServletResponse servletResponse,
            FilterChain filterChain
    ) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        String path = request.getRequestURI();
        boolean isPublic = isPublicEndpoint(path);
        if (isPublic) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }

        String token = TokenHandler.getTokenFromCookies(request, cookieAuthName);

        if (token != null && TokenHandler.validateToken(token, tokenKey)) {
            filterChain.doFilter(servletRequest, servletResponse);
        } else {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }
}
