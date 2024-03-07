package com.order.restaurant.api.controller;

import com.order.restaurant.api.exception.IncorrectOrderRequestException;
import com.order.restaurant.api.model.*;
import com.order.restaurant.api.model.request.FeedbackRequest;
import com.order.restaurant.api.model.request.ListRequest;
import com.order.restaurant.api.model.request.OrderRequest;
import com.order.restaurant.api.model.request.OrderUpdateRequest;
import com.order.restaurant.api.model.response.*;
import com.order.restaurant.api.service.OrderService;
import com.order.restaurant.api.service.security.handler.TokenHandler;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.expression.AccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final String tokenKey;
    private final String cookieAuthName;
    private final OrderService orderService;

    @Autowired
    public OrderController(OrderService orderService, Environment environment) {
        this.orderService = orderService;
        this.cookieAuthName = environment.getProperty("app.cookie-auth-name");
        this.tokenKey = environment.getProperty("app.token-key");
    }

    private Page<OrderResponse> getOrderResponses(@PathVariable String username, Pageable pageable) {
        List<OrderResponse> orderResponses = orderService.getUserOrders(username, pageable)
                .stream().map(order -> {
                    OrderResponse orderResponse = new OrderResponse();
                    orderResponse.setId(order.getId());
                    orderResponse.setDishes(order.getOrderDishes()
                            .stream().map(
                                    orderDish -> new OrderDishResponse(
                                            orderDish.getQuantity(),
                                            orderDish.getDish().getName(),
                                            orderDish.getDish().getPrice()
                                    )
                            ).toList());
                    orderResponse.setCreatedDateTime(order.getCreatedDateTime());
                    orderResponse.setStatus(order.getStatus());
                    FeedbackResponse feedbackResponse = new FeedbackResponse();
                    if (order.getFeedback() != null) {
                        feedbackResponse.setText(order.getFeedback().getText());
                        feedbackResponse.setRating(order.getFeedback().getRating());
                        orderResponse.setFeedback(feedbackResponse);
                    }

                    return orderResponse;
                }).collect(Collectors.toList());


        return PageableExecutionUtils.getPage(orderResponses, pageable, orderResponses::size);
    }

    private ResponseEntity<OrderResponse> getOrderResponse(Order order) {
        OrderResponse orderResponse = new OrderResponse();
        orderResponse.setDishes(order.getOrderDishes()
                .stream().map(orderDish -> new OrderDishResponse(
                                orderDish.getQuantity(),
                                orderDish.getDish().getName(),
                                orderDish.getDish().getPrice()
                        )
                ).toList());
        orderResponse.setId(order.getId());
        orderResponse.setStatus(order.getStatus());
        orderResponse.setCreatedDateTime(order.getCreatedDateTime());

        return ResponseEntity.ok().body(orderResponse);
    }

    private OrderDish buildOrderDish(Order order, OrderRequest orderRequest) {
        Dish dish;

        try {
            dish = orderService.getDish(orderRequest.dishId());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        OrderDish orderDish = new OrderDish();
        orderDish.setQuantity(orderRequest.count());
        orderDish.setDish(dish);
        orderDish.setOrder(order);

        return orderDish;
    }

    @GetMapping
    public Page<OrderResponse> getUserOrders(
            HttpServletRequest request,
            @RequestParam(value = "page", defaultValue = "0")
            int page,
            @RequestParam(value = "size", defaultValue = "30")
            int size
    ) {
        if (page < 0 || size < 0) {
            throw new IllegalArgumentException("Incorrect pageable params.");
        }

        Pageable pageable = PageRequest.of(page, size);
        String username = TokenHandler.getUsernameFromToken(
                TokenHandler.getTokenFromCookies(request, cookieAuthName),
                tokenKey
        );

        return getOrderResponses(username, pageable);
    }


    @PostMapping
    public ResponseEntity<OrderResponse> addOrder(
            HttpServletRequest request,
            @RequestBody
            @Valid
            ListRequest<OrderRequest> orderRequests
    ) throws Exception {
        String username = TokenHandler.getUsernameFromToken(TokenHandler.getTokenFromCookies(request, cookieAuthName), tokenKey);
        if (!orderRequests.getRequests().stream().allMatch(orderService::isCorrectOrder)) {
            throw new IncorrectOrderRequestException("Some dishes from the order do not exist or have run out");
        }

        Order order = new Order();
        order.setUser(orderService.getUser(username));
        order.setOrderDishes(orderRequests.getRequests()
                .stream().map(orderRequest -> buildOrderDish(order, orderRequest))
                .collect(Collectors.toCollection(ArrayList::new)));
        order.setCreatedDateTime(LocalDateTime.now());
        order.setStatus(Status.ACCEPT);

        orderService.addOrder(order);
        orderService.processOrder(order);

        return getOrderResponse(order);
    }

    @PutMapping
    public ResponseEntity<OrderResponse> changeOrder(
            HttpServletRequest request,
            @RequestBody
            @Valid
            OrderUpdateRequest orderUpdateRequest
    ) throws Exception {
        String username = TokenHandler.getUsernameFromToken(
                TokenHandler.getTokenFromCookies(request, cookieAuthName),
                tokenKey
        );

        if (!orderUpdateRequest.getOrderRequests().stream().allMatch(orderService::isCorrectOrder)) {
            throw new IncorrectOrderRequestException("Some dishes from the order do not exist or have run out");
        }

        if (!orderService.isCorrectUpdateOrder(orderUpdateRequest, username)) {
            throw new IncorrectOrderRequestException("Order cannot be changed");
        }

        Order order = new Order();
        order.setId(orderUpdateRequest.getOrderId());
        Order finalOrder = order;
        order.setOrderDishes(orderUpdateRequest.getOrderRequests()
                .stream().map(orderRequest -> buildOrderDish(finalOrder, orderRequest))
                .collect(Collectors.toCollection(ArrayList::new)));

        orderService.processOrder(orderService.updateOrder(order));

        Optional<Order> modifiedOrder = orderService.getOrder(orderUpdateRequest.getOrderId());
        if (modifiedOrder.isEmpty()) {
            throw new Exception(
                    String.format("Order with id = %d doesn't exist after updating",
                            orderUpdateRequest.getOrderId())
            );
        }

        order = modifiedOrder.get();

        return getOrderResponse(order);
    }


    @PostMapping("/pay{id}")
    public ResponseEntity<PaymentReceiptResponse> payForOrder(
            HttpServletRequest request,
            @PathVariable
            long id
    ) throws IncorrectOrderRequestException {
        String username = TokenHandler.getUsernameFromToken(
                TokenHandler.getTokenFromCookies(request, cookieAuthName),
                tokenKey
        );

        if (!orderService.canBePaid(id, username)) {
            throw new IncorrectOrderRequestException("Order cannot be paid");
        }

        Long total = orderService.payForOrder(id);
        PaymentReceiptResponse paymentReceiptResponse = new PaymentReceiptResponse();
        paymentReceiptResponse.setTotalReceipt(total);

        return ResponseEntity.ok(paymentReceiptResponse);
    }

    @PostMapping("/feedback{id}")
    public ResponseEntity<ApiResponse> reviewOrder(
            HttpServletRequest request,
            @PathVariable
            long id,
            @Valid
            @RequestBody
            FeedbackRequest feedbackRequest
    ) throws IncorrectOrderRequestException {
        String username = TokenHandler.getUsernameFromToken(
                TokenHandler.getTokenFromCookies(request, cookieAuthName),
                tokenKey
        );

        if (!orderService.canBeReviewed(id, username)) {
            throw new IncorrectOrderRequestException("Order cannot be paid");
        }

        Feedback feedback = new Feedback();
        feedback.setText(feedbackRequest.getText());
        feedback.setRating(feedbackRequest.getRating());
        orderService.reviewOrder(id, feedback);
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setDescription("The review was successfully left");

        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/{username}")
    public Page<OrderResponse> getAdminUserOrders(
            HttpServletRequest request,
            @RequestParam(value = "page", defaultValue = "0")
            int page,
            @RequestParam(value = "size", defaultValue = "30")
            int size,
            @PathVariable
            String username
    ) throws AccessException {
        if (page < 0 || size < 0) {
            throw new IllegalArgumentException("Incorrect pageable params.");
        }

        Pageable pageable = PageRequest.of(page, size);
        String login = TokenHandler.getUsernameFromToken(
                TokenHandler.getTokenFromCookies(request, cookieAuthName),
                tokenKey
        );

        if (!orderService.isAdmin(login)) {
            throw new AccessException("Permission denied.");
        }


        return getOrderResponses(username, pageable);
    }
}
