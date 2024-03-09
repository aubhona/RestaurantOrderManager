package com.order.restaurant.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Getter
@Entity
@Setter
@Table(name = "dishes")
public class Dish {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true)
    private String name;
    @Column
    private Long price;
    @Column
    private Long count;
    @Column
    private Long cookingDuration;
    @JsonIgnore
    @OneToMany(mappedBy = "dish")
    private List<OrderDish> orderDishes = new ArrayList<>();
}
