package com.restaurant.luch.services;

import com.restaurant.luch.models.*;
import com.restaurant.luch.repositories.*;
import java.util.ArrayList;
import java.util.List;

public class RestaurantService {
    private static RestaurantService instance;
    private static User currentUser;

    private RestaurantService() {}

    public static synchronized RestaurantService getInstance() {
        if (instance == null) {
            instance = new RestaurantService();
        }
        return instance;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static boolean isCurrentUserAdmin() {
        return currentUser != null && currentUser.isAdmin();
    }

    public static void logout() {
        currentUser = null;
    }

    // ===== DISHES =====
    public List<Dish> getAllDishes() throws Exception {
        return DishRepository.getAllDishes();
    }

    public Dish getDishById(int id) throws Exception {
        return DishRepository.getDishById(id);
    }

    public Dish addDish(Dish dish) throws Exception {
        return DishRepository.addDish(dish);
    }

    public void updateDish(Dish dish) throws Exception {
        DishRepository.updateDish(dish);
    }

    public void deleteDish(int dishId) throws Exception {
        DishRepository.deleteDish(dishId);
    }

    // ===== ORDERS =====
    public List<Order> getAllOrders() throws Exception {
        return OrderRepository.getAllOrders();
    }

    public List<Order> getAllOrdersAdmin() throws Exception {
        if (!isCurrentUserAdmin()) {
            throw new SecurityException("Доступ запрещён: требуются права администратора");
        }
        return OrderRepository.getAllOrdersAdmin();
    }

    public Order getOrderById(int id) throws Exception {
        return OrderRepository.getOrderById(id);
    }

    public Order addOrder(Order order) throws Exception {
        if (currentUser != null) {
            order.setUserId(currentUser.getId());
        } else {
            order.setUserId(null);
        }
        return OrderRepository.addOrder(order);
    }

    public void updateOrderStatus(int orderId, String newStatus) throws Exception {
        OrderRepository.updateOrderStatus(orderId, newStatus);
    }

    public void deleteOrder(int orderId) throws Exception {
        OrderRepository.deleteOrder(orderId);
    }

    public List<Order> getOrdersByUserId(int userId) throws Exception {
        return OrderRepository.getOrdersByUserId(userId);
    }

    // ===== BOOKINGS =====
    public List<Booking> getAllBookings() throws Exception {
        return BookingRepository.getAllBookings();
    }

    public List<Booking> getAllBookingsAdmin() throws Exception {
        if (!isCurrentUserAdmin()) {
            throw new SecurityException("Доступ запрещён: требуются права администратора");
        }
        return BookingRepository.getAllBookingsAdmin();
    }

    public Booking getBookingById(int id) throws Exception {
        return BookingRepository.getBookingById(id);
    }

    public Booking addBooking(Booking booking) throws Exception {
        if (currentUser != null) {
            booking.setUserId(currentUser.getId());
        } else {
            booking.setUserId(null);
        }
        return BookingRepository.addBooking(booking);
    }

    public void updateBookingStatus(int bookingId, String newStatus) throws Exception {
        BookingRepository.updateBookingStatus(bookingId, newStatus);
    }

    public void deleteBooking(int bookingId) throws Exception {
        BookingRepository.deleteBooking(bookingId);
    }

    public List<Booking> getBookingsByUserId(int userId) throws Exception {
        return BookingRepository.getBookingsByUserId(userId);
    }

    // ===== USERS =====
    public List<User> getAllUsers() throws Exception {
        return UserRepository.getAllUsers();
    }

    public User getUserById(int id) throws Exception {
        return UserRepository.getUserById(id);
    }

    public User getUserByEmail(String email) throws Exception {
        return UserRepository.getUserByEmail(email);
    }

    public User addUser(User user) throws Exception {
        return UserRepository.addUser(user);
    }

    public void updateUser(User user) throws Exception {
        UserRepository.updateUser(user);
    }

    public void deleteUser(int userId) throws Exception {
        UserRepository.deleteUser(userId);
    }

    // ===== FAVORITES =====
    public List<Dish> getFavoriteDishes(int userId) throws Exception {
        List<Favorite> favorites = FavoriteRepository.getFavoritesByUserId(userId);
        List<Dish> dishes = new ArrayList<>();
        for (Favorite fav : favorites) {
            Dish dish = getDishById(fav.getDishId());
            if (dish != null) dishes.add(dish);
        }
        return dishes;
    }

    public void addToFavorites(int userId, int dishId) throws Exception {
        FavoriteRepository.addFavorite(userId, dishId);
    }

    public void removeFromFavorites(int userId, int dishId) throws Exception {
        FavoriteRepository.removeFavorite(userId, dishId);
    }

    public boolean isFavorite(int userId, int dishId) throws Exception {
        return FavoriteRepository.isFavorite(userId, dishId);
    }
        // ===== ORDER ITEMS =====
        public void addOrderItems(List<OrderItem> items) throws Exception {
            OrderItemRepository.addOrderItems(items);
        }
    }
