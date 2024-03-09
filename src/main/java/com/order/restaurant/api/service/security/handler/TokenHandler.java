package com.order.restaurant.api.service.security.handler;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

public class TokenHandler {
    public static String getUsernameFromToken(String token, String tokenKey) {
        SecretKey secretKey = Keys.hmacShaKeyFor(tokenKey.getBytes(StandardCharsets.UTF_8));
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.getSubject();
    }

    public static String getTokenFromCookies(HttpServletRequest request, String cookieAuthName) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookieAuthName.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }

        return null;
    }

    public static boolean validateToken(String token, String tokenKey) {
        try {
            SecretKey secretKey = Keys.hmacShaKeyFor(tokenKey.getBytes(StandardCharsets.UTF_8));
            Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
