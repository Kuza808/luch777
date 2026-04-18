package com.restaurant.luch.services;

import com.restaurant.luch.models.Dish;
import java.util.LinkedHashMap;
import java.util.Map;

public class CartService {
    private static CartService instance;
    private final Map<Dish, Integer> cartItems = new LinkedHashMap<>();

    private CartService() {}

    public static synchronized CartService getInstance() {
        if (instance == null) {
            instance = new CartService();
        }
        return instance;
    }

    public Map<Dish, Integer> getCartItems() {
        return cartItems;
    }

    public int getQuantity(Dish dish) {
        return cartItems.getOrDefault(dish, 0);
    }

    public void addToCart(Dish dish) {
        cartItems.put(dish, getQuantity(dish) + 1);
    }

    public void removeOne(Dish dish) {
        int qty = getQuantity(dish);
        if (qty > 1) {
            cartItems.put(dish, qty - 1);
        } else {
            cartItems.remove(dish);
        }
    }

    public void removeCompletely(Dish dish) {
        cartItems.remove(dish);
    }

    public void clear() {
        cartItems.clear();
    }

    public double getTotalPrice() {
        return cartItems.entrySet().stream()
                .mapToDouble(e -> e.getKey().getPrice() * e.getValue())
                .sum();
    }

    public boolean isEmpty() {
        return cartItems.isEmpty();
    }
}