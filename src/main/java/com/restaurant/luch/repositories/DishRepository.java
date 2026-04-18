package com.restaurant.luch.repositories;

import com.restaurant.luch.config.AppConfig;
import com.restaurant.luch.models.Dish;
import com.restaurant.luch.utils.SupabaseApiClient;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class DishRepository {
    private static final String TABLE = AppConfig.TABLE_DISHES;

    public static List<Dish> getAllDishes() throws Exception {
        try {
            Dish[] dishes = SupabaseApiClient.get(TABLE, Dish[].class);
            System.out.println("DishRepository: загружено блюд: " + dishes.length);
            return Arrays.asList(dishes);
        } catch (IOException | InterruptedException e) {
            throw new Exception("Ошибка загрузки блюд: " + e.getMessage(), e);
        }
    }

    public static Dish getDishById(int id) throws Exception {
        try {
            Dish[] dishes = SupabaseApiClient.get(TABLE, "id", id, Dish[].class);
            return dishes.length > 0 ? dishes[0] : null;
        } catch (IOException | InterruptedException e) {
            throw new Exception("Ошибка загрузки блюда: " + e.getMessage(), e);
        }
    }

    public static Dish addDish(Dish dish) throws Exception {
        try {
            Dish[] created = SupabaseApiClient.adminPostForArray(TABLE, dish, Dish.class);
            if (created.length > 0) return created[0];
            else throw new Exception("Не удалось создать блюдо");
        } catch (IOException | InterruptedException e) {
            throw new Exception("Ошибка добавления блюда: " + e.getMessage(), e);
        }
    }

    public static void updateDish(Dish dish) throws Exception {
        try {
            SupabaseApiClient.adminPatch(TABLE, "id", dish.getId(), dish);
        } catch (IOException | InterruptedException e) {
            throw new Exception("Ошибка обновления блюда: " + e.getMessage(), e);
        }
    }

    public static void deleteDish(int id) throws Exception {
        try {
            SupabaseApiClient.adminDelete(TABLE, "id", id);
        } catch (IOException | InterruptedException e) {
            throw new Exception("Ошибка удаления блюда: " + e.getMessage(), e);
        }
    }
}