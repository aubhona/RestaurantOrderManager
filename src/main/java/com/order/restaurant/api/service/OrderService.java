package com.order.restaurant.api.service;

import com.order.restaurant.api.exception.CookingTimeException;
import com.order.restaurant.api.exception.IncorrectOrderRequestException;
import com.order.restaurant.api.model.*;
import com.order.restaurant.api.model.request.OrderRequest;
import com.order.restaurant.api.model.request.OrderUpdateRequest;
import com.order.restaurant.api.service.repository.DishRepository;
import com.order.restaurant.api.service.repository.OrderRepository;
import com.order.restaurant.api.service.repository.RevenueRepository;
import com.order.restaurant.api.service.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Service
public class OrderService {
    private static final Logger LOGGER = LoggerFactory.getLogger(OrderService.class);
    private static final long MILLISECONDS_IN_MINUTE = 60_000;
//    private static final long MILLISECONDS_IN_MINUTE = 1_000;
    private static final int DEFAULT_THREAD_COUNT = 5;

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final DishRepository dishRepository;
    private final ExecutorService executorService;
    private final HashMap<Long, Future<?>> cookingMap;
    private final RevenueRepository revenueRepository;

    @Autowired
    public OrderService(
            OrderRepository orderRepository,
            UserRepository userRepository,
            DishRepository dishRepository,
            RevenueRepository revenueRepository,
            Environment environment
    ) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.dishRepository = dishRepository;
        Integer threadCount = environment.getProperty(
                "app.thread-count",
                Integer.class
        );
        if (threadCount == null) {
            threadCount = DEFAULT_THREAD_COUNT;
        }
        this.executorService = Executors.newFixedThreadPool(threadCount);
        this.revenueRepository = revenueRepository;
        cookingMap = new HashMap<>();
    }

    private void clearTasks() {
        Iterator<Map.Entry<Long, Future<?>>> iterator = cookingMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Long, Future<?>> entry = iterator.next();
            Future<?> task = entry.getValue();
            if (task.isDone()) {
                task.cancel(false);
                iterator.remove();
            }
        }
        orderRepository.flush();
    }

    @Transactional
    public void decreaseDishes(Order order) {
        for (OrderDish orderDish : order.getOrderDishes()) {
            Dish changingDish =  orderDish.getDish();
            changingDish.setCount(changingDish.getCount() - orderDish.getQuantity());
            dishRepository.save(changingDish);
        }

        dishRepository.flush();
    }

    @Transactional
    public Page<Order> getUserOrders(String username, Pageable pageable) {
        clearTasks();

        return orderRepository.findByUsername(username, pageable);
    }

    @Transactional
    public void addOrder(Order order) {
        clearTasks();
        orderRepository.save(order);
        decreaseDishes(order);
    }

    @Transactional
    public User getUser(String username) {
        return userRepository.findUserByUsername(username);
    }

    @Transactional
    public Dish getDish(Long dishId) throws Exception {
        Optional<Dish> dish = dishRepository.findById(dishId);
        if (dish.isEmpty()) {
            LOGGER.error(String.format("Dish with id = %d doesn't exist.", dishId));
            throw new Exception(String.format("Dish with id = %d doesn't exist.", dishId));
        }

        return dish.get();
    }

    @Transactional
    public boolean isCorrectOrder(OrderRequest orderRequest) {
        Optional<Dish> dish = dishRepository.findById(orderRequest.dishId());
        return dish.isPresent() && dish.get().getCount() >= orderRequest.count();
    }

    @Transactional
    public void processOrder(Order order) throws CookingTimeException {
        long cookingTime = order.getOrderDishes().stream().mapToLong(orderDishes -> orderDishes.getQuantity() * orderDishes.getDish().getCookingDuration()).sum() * MILLISECONDS_IN_MINUTE ;
        if (cookingTime < 0) {
            throw new CookingTimeException("Cooking time is too long");
        }
        Future<?> orderProcessing = executorService.submit(() -> {
            try {
                cooking(order, cookingTime);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        cookingMap.put(order.getId(), orderProcessing);
    }

    @Transactional
    public void cooking(Order order, long cookingTime) throws InterruptedException {
        Order finalOrder = order;
        order = orderRepository.findById(order.getId()).orElse(finalOrder);
        order.setStatus(Status.COOKING);
        orderRepository.saveAndFlush(order);
        Thread.sleep(cookingTime);
        finalOrder = order;
        order = orderRepository.findById(order.getId()).orElse(finalOrder);
        order.setStatus(Status.READY);
        orderRepository.saveAndFlush(order);
    }

    @Transactional
    public Order updateOrder(Order order) throws Exception {
        clearTasks();
        if (cookingMap.containsKey(order.getId())) {
            cookingMap.get(order.getId()).cancel(true);
            cookingMap.remove(order.getId());
        }
        Optional<Order> modifyingOrder = orderRepository.findById(order.getId());
        if (modifyingOrder.isEmpty()) {
            LOGGER.error(String.format("Order with id = %d doesn't exist.", order.getId()));
            throw new Exception(String.format("Order with id = %d doesn't exist.", order.getId()));
        }

        modifyingOrder.get().getOrderDishes().clear();
        modifyingOrder.get().getOrderDishes().addAll(order.getOrderDishes());
        modifyingOrder.get().setStatus(Status.ACCEPT);
        decreaseDishes(modifyingOrder.get());
        orderRepository.saveAndFlush(modifyingOrder.get());

        return modifyingOrder.get();
    }

    @Transactional
    public boolean isCorrectUpdateOrder(OrderUpdateRequest orderUpdateRequest, String username) {
        Optional<Order> order = orderRepository.findById(orderUpdateRequest.getOrderId());
        return order.isPresent()
                && order.get().getStatus() != Status.READY
                && order.get().getStatus() != Status.PAID
                && order.get().getUser().getUsername().equals(username);
    }

    @Transactional
    public Optional<Order> getOrder(Long orderId) {
        return orderRepository.findById(orderId);
    }


    @Transactional
    public boolean canBePaid(Long orderId, String username) {
        clearTasks();
        Optional<Order> order = orderRepository.findById(orderId);
        return order.isPresent()
                && order.get().getStatus() == Status.READY
                && order.get().getUser().getUsername().equals(username);
    }

    @Transactional
    public Long payForOrder(Long orderId) throws IncorrectOrderRequestException {
        Optional<Order> order = orderRepository.findById(orderId);
        if (order.isEmpty()) {
            LOGGER.error(String.format("Order with id = %d doesn't exist", orderId));
            throw new IncorrectOrderRequestException(String.format("Order with id = %d doesn't exist", orderId));
        }
        order.get().setStatus(Status.PAID);
        orderRepository.save(order.get());

        Optional<Revenue> revenue = revenueRepository.findById(1L);
        if (revenue.isEmpty()) {
            revenue = Optional.of(new Revenue());
            revenue.get().setTotalAmount(BigDecimal.valueOf(0));
        }

        long bill = order.get().getOrderDishes().stream()
                .mapToLong(orderDish -> orderDish.getQuantity() * orderDish.getDish().getPrice())
                .sum();

        revenue.get().setTotalAmount(revenue.get().getTotalAmount().add(BigDecimal.valueOf(bill)));

        revenueRepository.save(revenue.get());

        return bill;
    }

    @Transactional
    public boolean canBeReviewed(Long orderId, String username) {
        clearTasks();
        Optional<Order> order = orderRepository.findById(orderId);
        return order.isPresent()
                && order.get().getStatus() == Status.PAID
                && order.get().getUser().getUsername().equals(username)
                && order.get().getFeedback() == null;
    }

    @Transactional
    public void reviewOrder(Long orderId, Feedback feedback) throws IncorrectOrderRequestException {
        Optional<Order> order = orderRepository.findById(orderId);
        if (order.isEmpty()) {
            LOGGER.error(String.format("Order with id = %d doesn't exist", orderId));
            throw new IncorrectOrderRequestException(String.format("Order with id = %d doesn't exist", orderId));
        }
        feedback.setOrder(order.get());
        order.get().setFeedback(feedback);

        orderRepository.save(order.get());
    }

    @Transactional
    public boolean isAdmin(String username) {
        User user = userRepository.findUserByUsername(username);
        return user != null && user.getRole() == Role.ADMIN;
    }
}
