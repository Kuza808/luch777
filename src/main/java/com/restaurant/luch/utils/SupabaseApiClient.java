package com.restaurant.luch.utils;

import com.restaurant.luch.supabase.SupabaseClient;
import java.io.IOException;
import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.lang.reflect.Type;
import java.util.Map;
import com.google.gson.reflect.TypeToken;

public class SupabaseApiClient {
    private static final SupabaseClient client = SupabaseClient.getInstance();
    private static final int MAX_RETRIES = 5;

    // ========== Обычные запросы (анонимный ключ) ==========
    public static <T> T get(String table, Class<T> responseType) throws IOException, InterruptedException {
        HttpRequest request = client.createRequest("/" + table).GET().build();
        return sendWithRetry(request, responseType, MAX_RETRIES);
    }

    public static <T> T get(String table, String filterColumn, Object filterValue, Class<T> responseType) throws IOException, InterruptedException {
        String encodedValue = URLEncoder.encode(filterValue.toString(), StandardCharsets.UTF_8);
        String query = "?" + filterColumn + "=eq." + encodedValue;
        HttpRequest request = client.createRequest("/" + table + query).GET().build();
        return sendWithRetry(request, responseType, MAX_RETRIES);
    }

    public static <T> T[] postForArray(String table, Object body, Class<T> elementType) throws IOException, InterruptedException {
        String jsonBody = client.getGson().toJson(body);
        HttpRequest request = client.createRequest("/" + table)
                .header("Prefer", "return=representation")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();
        HttpResponse<String> response = sendRequestRaw(request, MAX_RETRIES);
        Type arrayType = TypeToken.getArray(elementType).getType();
        return client.getGson().fromJson(response.body(), arrayType);
    }

    public static void post(String table, Object body) throws IOException, InterruptedException {
        String jsonBody = client.getGson().toJson(body);
        HttpRequest request = client.createRequest("/" + table)
                .header("Prefer", "return=minimal")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();
        sendRequestRaw(request, MAX_RETRIES);
    }

    public static <T> T patch(String table, String filterColumn, Object filterValue, Object body, Class<T> responseType) throws IOException, InterruptedException {
        String encodedValue = URLEncoder.encode(filterValue.toString(), StandardCharsets.UTF_8);
        String query = "?" + filterColumn + "=eq." + encodedValue;
        String jsonBody = client.getGson().toJson(body);
        HttpRequest request = client.createRequest("/" + table + query)
                .method("PATCH", HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();
        return sendWithRetry(request, responseType, MAX_RETRIES);
    }

    public static void patch(String table, String filterColumn, Object filterValue, Object body) throws IOException, InterruptedException {
        String encodedValue = URLEncoder.encode(filterValue.toString(), StandardCharsets.UTF_8);
        String query = "?" + filterColumn + "=eq." + encodedValue;
        String jsonBody = client.getGson().toJson(body);
        HttpRequest request = client.createRequest("/" + table + query)
                .header("Prefer", "return=minimal")
                .method("PATCH", HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();
        sendRequestRaw(request, MAX_RETRIES);
    }

    public static void patch(String table, String filterColumn, Object filterValue, Map<String, Object> updates) throws IOException, InterruptedException {
        String encodedValue = URLEncoder.encode(filterValue.toString(), StandardCharsets.UTF_8);
        String query = "?" + filterColumn + "=eq." + encodedValue;
        String jsonBody = client.getGson().toJson(updates);
        HttpRequest request = client.createRequest("/" + table + query)
                .header("Prefer", "return=minimal")
                .method("PATCH", HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();
        sendRequestRaw(request, MAX_RETRIES);
    }

    public static void delete(String table, String filterColumn, Object filterValue) throws IOException, InterruptedException {
        String encodedValue = URLEncoder.encode(filterValue.toString(), StandardCharsets.UTF_8);
        String query = "?" + filterColumn + "=eq." + encodedValue;
        HttpRequest request = client.createRequest("/" + table + query).DELETE().build();
        sendRequestRaw(request, MAX_RETRIES);
    }

    // ========== Админские запросы (сервисный ключ) ==========
    public static <T> T adminGet(String table, Class<T> responseType) throws IOException, InterruptedException {
        HttpRequest request = client.createAdminRequest("/" + table).GET().build();
        return sendWithRetry(request, responseType, MAX_RETRIES);
    }

    public static <T> T adminGet(String table, String filterColumn, Object filterValue, Class<T> responseType) throws IOException, InterruptedException {
        String encodedValue = URLEncoder.encode(filterValue.toString(), StandardCharsets.UTF_8);
        String query = "?" + filterColumn + "=eq." + encodedValue;
        HttpRequest request = client.createAdminRequest("/" + table + query).GET().build();
        return sendWithRetry(request, responseType, MAX_RETRIES);
    }

    public static <T> T adminPatch(String table, String filterColumn, Object filterValue, Object body, Class<T> responseType) throws IOException, InterruptedException {
        String encodedValue = URLEncoder.encode(filterValue.toString(), StandardCharsets.UTF_8);
        String query = "?" + filterColumn + "=eq." + encodedValue;
        String jsonBody = client.getGson().toJson(body);
        HttpRequest request = client.createAdminRequest("/" + table + query)
                .method("PATCH", HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();
        return sendWithRetry(request, responseType, MAX_RETRIES);
    }

    public static void adminPatch(String table, String filterColumn, Object filterValue, Object body) throws IOException, InterruptedException {
        String encodedValue = URLEncoder.encode(filterValue.toString(), StandardCharsets.UTF_8);
        String query = "?" + filterColumn + "=eq." + encodedValue;
        String jsonBody = client.getGson().toJson(body);
        HttpRequest request = client.createAdminRequest("/" + table + query)
                .header("Prefer", "return=minimal")
                .method("PATCH", HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();
        sendRequestRaw(request, MAX_RETRIES);
    }

    public static void adminPatch(String table, String filterColumn, Object filterValue, Map<String, Object> updates) throws IOException, InterruptedException {
        String encodedValue = URLEncoder.encode(filterValue.toString(), StandardCharsets.UTF_8);
        String query = "?" + filterColumn + "=eq." + encodedValue;
        String jsonBody = client.getGson().toJson(updates);
        HttpRequest request = client.createAdminRequest("/" + table + query)
                .header("Prefer", "return=minimal")
                .method("PATCH", HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();
        sendRequestRaw(request, MAX_RETRIES);
    }

    public static <T> T[] adminPostForArray(String table, Object body, Class<T> elementType) throws IOException, InterruptedException {
        String jsonBody = client.getGson().toJson(body);
        HttpRequest request = client.createAdminRequest("/" + table)
                .header("Prefer", "return=representation")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();
        HttpResponse<String> response = sendRequestRaw(request, MAX_RETRIES);
        Type arrayType = TypeToken.getArray(elementType).getType();
        return client.getGson().fromJson(response.body(), arrayType);
    }

    public static void adminPost(String table, Object body) throws IOException, InterruptedException {
        String jsonBody = client.getGson().toJson(body);
        HttpRequest request = client.createAdminRequest("/" + table)
                .header("Prefer", "return=minimal")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();
        sendRequestRaw(request, MAX_RETRIES);
    }

    public static void adminDelete(String table, String filterColumn, Object filterValue) throws IOException, InterruptedException {
        String encodedValue = URLEncoder.encode(filterValue.toString(), StandardCharsets.UTF_8);
        String query = "?" + filterColumn + "=eq." + encodedValue;
        HttpRequest request = client.createAdminRequest("/" + table + query).DELETE().build();
        sendRequestRaw(request, MAX_RETRIES);
    }

    // ========== Вспомогательные методы ==========
    private static <T> T sendWithRetry(HttpRequest request, Class<T> responseType, int maxRetries) throws IOException, InterruptedException {
        int attempt = 0;
        while (true) {
            try {
                return sendRequest(request, responseType);
            } catch (IOException e) {
                attempt++;
                if (attempt > maxRetries) throw e;
                long delay = (long) Math.pow(2, attempt) * 1000;
                System.err.println("Сетевая ошибка, повтор " + attempt + " из " + maxRetries + " через " + delay + " мс: " + e.getMessage());
                Thread.sleep(delay);
            }
        }
    }

    public static <T> T sendRequest(HttpRequest request, Class<T> responseType) throws IOException, InterruptedException {
        HttpResponse<String> response = sendRequestRaw(request, 1);
        return client.getGson().fromJson(response.body(), responseType);
    }

    private static HttpResponse<String> sendRequestRaw(HttpRequest request, int retries) throws IOException, InterruptedException {
        System.out.println("🌐 Отправка запроса: " + request.uri());
        try {
            HttpResponse<String> response = client.getHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("📡 Статус ответа: " + response.statusCode());
            if (response.statusCode() >= 400) {
                System.err.println("❌ Тело ответа (ошибка): " + response.body());
                throw new IOException("Supabase API error " + response.statusCode() + ": " + response.body());
            }
            return response;
        } catch (IOException e) {
            System.err.println("❌ Исключение при отправке: " + e.getClass().getName() + " - " + e.getMessage());
            throw e;
        }
    }
}