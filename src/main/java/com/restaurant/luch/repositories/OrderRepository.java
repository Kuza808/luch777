package com.restaurant.luch.repositories;

import com.restaurant.luch.config.AppConfig;
import com.restaurant.luch.models.Order;
import com.restaurant.luch.utils.SupabaseApiClient;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class OrderRepository {
    private static final String TABLE = AppConfig.TABLE_ORDERS;

    public static List<Order> getAllOrders() throws Exception {
        try {
            Order[] orders = SupabaseApiClient.get(TABLE, Order[].class);
            return Arrays.asList(orders);
        } catch (IOException | InterruptedException e) {
            throw new Exception("Ошибка загрузки заказов: " + e.getMessage(), e);
        }
    }

    public static List<Order> getAllOrdersAdmin() throws Exception {
        try {
            Order[] orders = SupabaseApiClient.adminGet(TABLE, Order[].class);
            return Arrays.asList(orders);
        } catch (IOException | InterruptedException e) {
            throw new Exception("Ошибка загрузки заказов (админ): " + e.getMessage(), e);
        }
    }

    public static Order getOrderById(int id) throws Exception {
        try {
            Order[] orders = SupabaseApiClient.get(TABLE, "id", id, Order[].class);
            return orders.length > 0 ? orders[0] : null;
        } catch (IOException | InterruptedException e) {
            throw new Exception("Ошибка загрузки заказа: " + e.getMessage(), e);
        }
    }

    public static List<Order> getOrdersByUserId(int userId) throws Exception {
        try {
            // 🔁 ИСПРАВЛЕНИЕ: используем adminGet вместо get
            Order[] orders = SupabaseApiClient.adminGet(TABLE, "user_id", userId, Order[].class);
            return Arrays.asList(orders);
        } catch (IOException | InterruptedException e) {
            throw new Exception("Ошибка загрузки заказов пользователя: " + e.getMessage(), e);
        }
    }

    public static Order addOrder(Order order) throws Exception {
        try {
            Order[] created = SupabaseApiClient.adminPostForArray(TABLE, order, Order.class);
            if (created.length > 0) return created[0];
            else throw new Exception("Не удалось создать заказ");
        } catch (IOException | InterruptedException e) {
            throw new Exception("Ошибка добавления заказа: " + e.getMessage(), e);
        }
    }

    public static void updateOrderStatus(int orderId, String newStatus) throws Exception {
        try {
            Order patch = new Order();
            patch.setStatus(newStatus);
            SupabaseApiClient.adminPatch(TABLE, "id", orderId, patch);
        } catch (IOException | InterruptedException e) {
            throw new Exception("Ошибка обновления статуса заказа: " + e.getMessage(), e);
        }
    }

    public static void deleteOrder(int orderId) throws Exception {
        try {
            SupabaseApiClient.adminDelete(TABLE, "id", orderId);
        } catch (IOException | InterruptedException e) {
            throw new Exception("Ошибка удаления заказа: " + e.getMessage(), e);
        }
    }
}