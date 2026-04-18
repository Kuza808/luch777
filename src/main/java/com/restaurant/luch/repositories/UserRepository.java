package com.restaurant.luch.repositories;

import com.restaurant.luch.config.AppConfig;
import com.restaurant.luch.models.User;
import com.restaurant.luch.utils.SupabaseApiClient;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class UserRepository {
    private static final String TABLE = AppConfig.TABLE_USERS;

    public static List<User> getAllUsers() throws Exception {
        try {
            User[] users = SupabaseApiClient.get(TABLE, User[].class);
            return Arrays.asList(users);
        } catch (IOException | InterruptedException e) {
            throw new Exception("Ошибка загрузки пользователей: " + e.getMessage(), e);
        }
    }

    public static User getUserById(int id) throws Exception {
        try {
            User[] users = SupabaseApiClient.get(TABLE, "id", id, User[].class);
            return users.length > 0 ? users[0] : null;
        } catch (IOException | InterruptedException e) {
            throw new Exception("Ошибка загрузки пользователя: " + e.getMessage(), e);
        }
    }

    public static User getUserByEmail(String email) throws Exception {
        try {
            User[] users = SupabaseApiClient.get(TABLE, "email", email, User[].class);
            return users.length > 0 ? users[0] : null;
        } catch (IOException | InterruptedException e) {
            throw new Exception("Ошибка загрузки пользователя по email: " + e.getMessage(), e);
        }
    }

    public static User addUser(User user) throws Exception {
        try {
            User[] createdUsers = SupabaseApiClient.postForArray(TABLE, user, User.class);
            if (createdUsers.length == 0) {
                throw new Exception("Пользователь не был создан (пустой ответ)");
            }
            return getUserByEmail(user.getEmail());
        } catch (IOException | InterruptedException e) {
            throw new Exception("Ошибка добавления пользователя: " + e.getMessage(), e);
        }
    }

    public static void updateUser(User user) throws Exception {
        try {
            SupabaseApiClient.adminPatch(TABLE, "id", user.getId(), user);
        } catch (IOException | InterruptedException e) {
            throw new Exception("Ошибка обновления пользователя: " + e.getMessage(), e);
        }
    }

    public static void deleteUser(int userId) throws Exception {
        try {
            SupabaseApiClient.adminDelete(TABLE, "id", userId);
        } catch (IOException | InterruptedException e) {
            throw new Exception("Ошибка удаления пользователя: " + e.getMessage(), e);
        }
    }
}