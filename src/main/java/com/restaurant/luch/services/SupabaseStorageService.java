package com.restaurant.luch.services;

import com.restaurant.luch.config.AppConfig;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;

public class SupabaseStorageService {
    private static final String BUCKET_NAME = AppConfig.SUPABASE_STORAGE_BUCKET;
    private static final String STORAGE_URL = AppConfig.SUPABASE_URL + "/storage/v1/object/" + BUCKET_NAME;
    private static final String SERVICE_KEY = AppConfig.SUPABASE_SERVICE_KEY;
    private static final HttpClient httpClient = HttpClient.newHttpClient();

    public static String uploadImage(File file, String fileName) throws Exception {
        if (!file.exists()) throw new IllegalArgumentException("Файл не найден");

        String filePath = "dishes/" + fileName + "_" + System.currentTimeMillis() + getFileExtension(file.getName());
        byte[] fileBytes = Files.readAllBytes(file.toPath());

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(STORAGE_URL + "/" + filePath))
                .header("apikey", SERVICE_KEY)
                .header("Authorization", "Bearer " + SERVICE_KEY)
                .header("Content-Type", Files.probeContentType(file.toPath()))
                .POST(HttpRequest.BodyPublishers.ofByteArray(fileBytes))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 400) {
            throw new IOException("Ошибка загрузки: " + response.body());
        }

        return AppConfig.SUPABASE_URL + "/storage/v1/object/public/" + BUCKET_NAME + "/" + filePath;
    }

    private static String getFileExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        return lastDot > 0 ? filename.substring(lastDot) : ".jpg";
    }
}