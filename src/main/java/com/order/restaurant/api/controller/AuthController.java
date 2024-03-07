package com.order.restaurant.api.controller;

import com.order.restaurant.api.exception.AuthorizationException;
import com.order.restaurant.api.exception.RegistrationException;
import com.order.restaurant.api.model.Role;
import com.order.restaurant.api.model.request.AuthorizationRequest;
import com.order.restaurant.api.model.request.RegistrationRequest;
import com.order.restaurant.api.model.response.ApiResponse;
import com.order.restaurant.api.service.AuthService;
import com.order.restaurant.api.service.security.hasher.MD5Encoder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Objects;

@RestController
@RequestMapping("api")
public class AuthController {
    private final Long sessionDuration;
    private final String tokenKey;
    private final String secretKey;
    private final String cookieAuthName;
    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService, Environment environment) {
        this.authService = authService;
        this.cookieAuthName = environment.getProperty("app.cookie-auth-name");
        this.tokenKey = environment.getProperty("app.token-key");
        this.secretKey = environment.getProperty("app.secret-key");
        this.sessionDuration = environment.getProperty("app.session-duration", Long.class);
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse> registerUser(
            @RequestBody
            @Valid
            RegistrationRequest request
    ) throws RegistrationException {
        if (authService.registerUser(
                request.getUsername(),
                MD5Encoder.getMD5Hash(request.getPassword()),
                Objects.equals(request.getSecretKey(), this.secretKey) ? Role.ADMIN : Role.USER)
        ) {
            ApiResponse response = new ApiResponse();
            response.setDescription("Successful registration");
            return ResponseEntity.ok(response);
        }

        throw new RegistrationException("A user with this username exists");
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(
            @RequestBody
            @Valid
            AuthorizationRequest request,
            HttpServletResponse response) throws AuthorizationException {
        SecretKey secretKey = Keys.hmacShaKeyFor(tokenKey.getBytes(StandardCharsets.UTF_8));
        if (authService.loginUser(request.getUsername(), MD5Encoder.getMD5Hash(request.getPassword()))) {
            String token = Jwts.builder()
                    .setSubject(request.getUsername())
                    .setIssuedAt(new Date())
                    .setExpiration(new Date(System.currentTimeMillis() + sessionDuration))
                    .signWith(secretKey, SignatureAlgorithm.HS256)
                    .compact();
            Cookie cookie = new Cookie(cookieAuthName, token);
            cookie.setHttpOnly(true);
            cookie.setPath("/");
            response.addCookie(cookie);
            ApiResponse apiResponse = new ApiResponse();
            apiResponse.setDescription("User successfully authenticated");

            return ResponseEntity.ok(apiResponse);
        }

        throw new AuthorizationException("Incorrect login or password");
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser(HttpServletResponse response) {
        Cookie cookie = new Cookie(cookieAuthName, null);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setDescription("Logged out successfully");

        return ResponseEntity.ok(apiResponse);
    }
}
