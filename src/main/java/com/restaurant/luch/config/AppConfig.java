package com.restaurant.luch.config;

public class AppConfig {

    // ========== SUPABASE CONFIGURATION ==========
    public static final String SUPABASE_URL = "https://ngbwvuhzkltfhahbrfvy.supabase.co";
    public static final String SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im5nYnd2dWh6a2x0ZmhhaGJyZnZ5Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjQ2MTIxNzAsImV4cCI6MjA4MDE4ODE3MH0.M8-ej3zAlWFu8XkR_fj6BZ7tM2iIdebdsofTgoE-za8";
    public static final String SUPABASE_STORAGE_BUCKET = "dish-images";

    // 🔐 СЕРВИСНЫЙ КЛЮЧ (service_role) – необходим для админских операций, обходящих RLS
    // ЗАМЕНИТЕ НА РЕАЛЬНЫЙ КЛЮЧ ИЗ НАСТРОЕК SUPABASE!
    public static final String SUPABASE_SERVICE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im5nYnd2dWh6a2x0ZmhhaGJyZnZ5Iiwicm9sZSI6InNlcnZpY2Vfcm9sZSIsImlhdCI6MTc2NDYxMjE3MCwiZXhwIjoyMDgwMTg4MTcwfQ.IYAUwtVrJqvaiYR9za5bGARu8jas5hdW_yzNFj19AQQ";

    // ========== APPLICATION CONFIGURATION ==========
    public static final String APP_NAME = "Ресторан 'Луч' - Система управления";
    public static final String APP_VERSION = "1.0.0";
    public static final int DEFAULT_WINDOW_WIDTH = 1400;
    public static final int DEFAULT_WINDOW_HEIGHT = 900;

    // ========== DATABASE TABLES ==========
    public static final String TABLE_USERS = "users";
    public static final String TABLE_DISHES = "dishes";
    public static final String TABLE_ORDERS = "orders";
    public static final String TABLE_ORDER_ITEMS = "order_items";
    public static final String TABLE_BOOKINGS = "bookings";

    // ========== UI CONFIGURATION ==========
    public static final int DISHES_PER_PAGE = 12;
    public static final int DISH_IMAGE_WIDTH = 180;
    public static final int DISH_IMAGE_HEIGHT = 120;
    public static final int DISH_CARD_WIDTH = 200;
    public static final int DISH_CARD_HEIGHT = 320;

    // ========== TIMING ==========
    public static final int NETWORK_TIMEOUT_MS = 30000;
    public static final int REFRESH_INTERVAL_MS = 30000;

    // ========== CONSTRAINTS ==========
    public static final double MIN_DISH_PRICE = 50.0;
    public static final double MAX_DISH_PRICE = 5000.0;
    public static final int MIN_COOKING_TIME = 5;
    public static final int MAX_COOKING_TIME = 120;

    // ========== ORDER STATUSES ==========
    public static final String ORDER_STATUS_PENDING = "PENDING";
    public static final String ORDER_STATUS_CONFIRMED = "CONFIRMED";
    public static final String ORDER_STATUS_PREPARING = "PREPARING";
    public static final String ORDER_STATUS_READY = "READY";
    public static final String ORDER_STATUS_COMPLETED = "COMPLETED";
    public static final String ORDER_STATUS_CANCELLED = "CANCELLED";

    // ========== ORDER TYPES ==========
    public static final String ORDER_TYPE_DINE_IN = "DINE_IN";
    public static final String ORDER_TYPE_TAKEOUT = "TAKEOUT";
    public static final String ORDER_TYPE_DELIVERY = "DELIVERY";

    // ========== BOOKING STATUSES ==========
    public static final String BOOKING_STATUS_PENDING = "PENDING";
    public static final String BOOKING_STATUS_CONFIRMED = "CONFIRMED";
    public static final String BOOKING_STATUS_SEATED = "SEATED";
    public static final String BOOKING_STATUS_COMPLETED = "COMPLETED";
    public static final String BOOKING_STATUS_CANCELLED = "CANCELLED";

    public static String[] getAllOrderStatuses() {
        return new String[]{
                ORDER_STATUS_PENDING,
                ORDER_STATUS_CONFIRMED,
                ORDER_STATUS_PREPARING,
                ORDER_STATUS_READY,
                ORDER_STATUS_COMPLETED,
                ORDER_STATUS_CANCELLED
        };
    }

    public static String[] getAllOrderTypes() {
        return new String[]{
                ORDER_TYPE_DINE_IN,
                ORDER_TYPE_TAKEOUT,
                ORDER_TYPE_DELIVERY
        };
    }

    public static String[] getAllBookingStatuses() {
        return new String[]{
                BOOKING_STATUS_PENDING,
                BOOKING_STATUS_CONFIRMED,
                BOOKING_STATUS_SEATED,
                BOOKING_STATUS_COMPLETED,
                BOOKING_STATUS_CANCELLED
        };
    }

    public static boolean isSupabaseConfigured() {
        return !SUPABASE_URL.contains("your-project") &&
                !SUPABASE_ANON_KEY.contains("your-anon-key");
    }

    public static boolean isServiceKeyConfigured() {
        return SUPABASE_SERVICE_KEY != null && !SUPABASE_SERVICE_KEY.contains("XXX") && !SUPABASE_SERVICE_KEY.isEmpty();
    }

    public static String getConfigurationError() {
        if (SUPABASE_URL.contains("your-project")) {
            return "⚠️ SUPABASE_URL не настроена в AppConfig.java";
        }
        if (SUPABASE_ANON_KEY.contains("your-anon-key")) {
            return "⚠️ SUPABASE_ANON_KEY не настроена в AppConfig.java";
        }
        if (!isServiceKeyConfigured()) {
            return "⚠️ SUPABASE_SERVICE_KEY не настроен в AppConfig.java (необходим для админ-панели)";
        }
        return null;
    }
}