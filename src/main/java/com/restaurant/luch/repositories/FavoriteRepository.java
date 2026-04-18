package com.restaurant.luch.repositories;

import com.restaurant.luch.models.Favorite;
import com.restaurant.luch.utils.SupabaseApiClient;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class FavoriteRepository {
    private static final String TABLE = "favorites";

    public static List<Favorite> getFavoritesByUserId(int userId) throws Exception {
        try {
            // Используем adminGet для обхода RLS
            Favorite[] favorites = SupabaseApiClient.adminGet(TABLE, "user_id", userId, Favorite[].class);
            return Arrays.asList(favorites);
        } catch (IOException | InterruptedException e) {
            throw new Exception("Ошибка загрузки избранного: " + e.getMessage(), e);
        }
    }

    public static void addFavorite(int userId, int dishId) throws Exception {
        Favorite favorite = new Favorite();
        favorite.setUserId(userId);
        favorite.setDishId(dishId);
        try {
            SupabaseApiClient.adminPost(TABLE, favorite);
        } catch (IOException | InterruptedException e) {
            throw new Exception("Ошибка добавления в избранное: " + e.getMessage(), e);
        }
    }

    public static void removeFavorite(int userId, int dishId) throws Exception {
        try {
            // Загружаем все избранные записи пользователя через adminGet
            Favorite[] favorites = SupabaseApiClient.adminGet(TABLE, "user_id", userId, Favorite[].class);
            for (Favorite fav : favorites) {
                if (fav.getDishId() == dishId) {
                    SupabaseApiClient.adminDelete(TABLE, "id", fav.getId());
                    break;
                }
            }
        } catch (IOException | InterruptedException e) {
            throw new Exception("Ошибка удаления из избранного: " + e.getMessage(), e);
        }
    }

    public static boolean isFavorite(int userId, int dishId) throws Exception {
        try {
            // Загружаем все избранные записи пользователя через adminGet
            Favorite[] favorites = SupabaseApiClient.adminGet(TABLE, "user_id", userId, Favorite[].class);
            for (Favorite fav : favorites) {
                if (fav.getDishId() == dishId) return true;
            }
            return false;
        } catch (IOException | InterruptedException e) {
            throw new Exception("Ошибка проверки избранного: " + e.getMessage(), e);
        }
    }
}