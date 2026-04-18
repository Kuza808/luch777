package com.restaurant.luch.models;

public class CartItem {
    private Dish dish;
    private int quantity;
    private double priceAtAdding;

    public CartItem() {}

    public CartItem(Dish dish, int quantity) {
        this.dish = dish;
        this.quantity = quantity;
        this.priceAtAdding = dish.getPrice();
    }

    public Dish getDish() { return dish; }
    public void setDish(Dish dish) { this.dish = dish; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public double getPriceAtAdding() { return priceAtAdding; }
    public void setPriceAtAdding(double priceAtAdding) { this.priceAtAdding = priceAtAdding; }

    public double getItemTotal() {
        return priceAtAdding * quantity;
    }

    public String getDishName() {
        return dish != null ? dish.getName() : "Unknown";
    }

    @Override
    public String toString() {
        return String.format("%s x%d = %.0f ₽", getDishName(), quantity, getItemTotal());
    }
}