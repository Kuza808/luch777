package com.restaurant.luch.models;

import com.google.gson.annotations.SerializedName;

public class OrderItem {
    @SerializedName("id")
    private Integer id;

    @SerializedName("order_id")
    private int orderId;

    @SerializedName("dish_id")
    private int dishId;

    @SerializedName("quantity")
    private int quantity;

    @SerializedName("price_at_order")
    private double priceAtOrder;

    public OrderItem() {}

    public OrderItem(Dish dish, int quantity) {
        this.dishId = dish.getId();
        this.quantity = quantity;
        this.priceAtOrder = dish.getPrice();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public int getDishId() {
        return dishId;
    }

    public void setDishId(int dishId) {
        this.dishId = dishId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getPriceAtOrder() {
        return priceAtOrder;
    }

    public void setPriceAtOrder(double priceAtOrder) {
        this.priceAtOrder = priceAtOrder;
    }

    public double getItemTotal() {
        return priceAtOrder * quantity;
    }
}