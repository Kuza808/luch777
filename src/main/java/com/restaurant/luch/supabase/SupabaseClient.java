package com.restaurant.luch.supabase;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.restaurant.luch.config.AppConfig;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class SupabaseClient {
    private static SupabaseClient instance;
    private final HttpClient httpClient;
    private final Gson gson;
    private final String supabaseUrl;
    private final String supabaseAnonKey;
    private final String supabaseServiceKey;

    private SupabaseClient() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(60))   // увеличен таймаут соединения
                .build();
        this.gson = new GsonBuilder()
                .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
                .registerTypeAdapter(LocalTime.class, new LocalTimeAdapter())
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .create();
        this.supabaseUrl = AppConfig.SUPABASE_URL;
        this.supabaseAnonKey = AppConfig.SUPABASE_ANON_KEY;
        this.supabaseServiceKey = AppConfig.SUPABASE_SERVICE_KEY;
    }

    public static synchronized SupabaseClient getInstance() {
        if (instance == null) {
            instance = new SupabaseClient();
        }
        return instance;
    }

    public HttpRequest.Builder createRequest(String path) {
        return buildRequest(path, supabaseAnonKey);
    }

    public HttpRequest.Builder createAdminRequest(String path) {
        if (supabaseServiceKey == null || supabaseServiceKey.isEmpty()) {
            throw new IllegalStateException("SUPABASE_SERVICE_KEY не задан в AppConfig");
        }
        return buildRequest(path, supabaseServiceKey);
    }

    private HttpRequest.Builder buildRequest(String path, String key) {
        return HttpRequest.newBuilder()
                .uri(URI.create(supabaseUrl + "/rest/v1" + path))
                .header("apikey", key)
                .header("Authorization", "Bearer " + key)
                .header("Content-Type", "application/json")
                .header("Prefer", "return=representation")
                .timeout(Duration.ofSeconds(120)); // увеличен таймаут запроса
    }

    public HttpClient getHttpClient() { return httpClient; }
    public Gson getGson() { return gson; }
}