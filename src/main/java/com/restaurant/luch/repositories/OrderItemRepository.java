package com.restaurant.luch.repositories;

import com.restaurant.luch.config.AppConfig;
import com.restaurant.luch.models.OrderItem;
import com.restaurant.luch.utils.SupabaseApiClient;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class OrderItemRepository {
    private static final String TABLE = AppConfig.TABLE_ORDER_ITEMS;

    public static List<OrderItem> addOrderItems(List<OrderItem> items) throws Exception {
        try {
            OrderItem[] created = SupabaseApiClient.adminPostForArray(TABLE, items, OrderItem.class);
            return Arrays.asList(created);
        } catch (IOException | InterruptedException e) {
            throw new Exception("Ошибка добавления позиций заказа: " + e.getMessage(), e);
        }
    }
}