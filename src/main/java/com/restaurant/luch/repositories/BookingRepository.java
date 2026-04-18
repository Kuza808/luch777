package com.restaurant.luch.repositories;

import com.restaurant.luch.config.AppConfig;
import com.restaurant.luch.models.Booking;
import com.restaurant.luch.utils.SupabaseApiClient;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

public class BookingRepository {
    private static final String TABLE = AppConfig.TABLE_BOOKINGS;

    public static List<Booking> getAllBookings() throws Exception {
        try {
            Booking[] bookings = SupabaseApiClient.get(TABLE, Booking[].class);
            return Arrays.asList(bookings);
        } catch (IOException | InterruptedException e) {
            throw new Exception("Ошибка загрузки бронирований: " + e.getMessage(), e);
        }
    }

    public static List<Booking> getAllBookingsAdmin() throws Exception {
        try {
            Booking[] bookings = SupabaseApiClient.adminGet(TABLE, Booking[].class);
            return Arrays.asList(bookings);
        } catch (IOException | InterruptedException e) {
            throw new Exception("Ошибка загрузки бронирований (админ): " + e.getMessage(), e);
        }
    }

    public static Booking getBookingById(int id) throws Exception {
        try {
            Booking[] bookings = SupabaseApiClient.get(TABLE, "id", id, Booking[].class);
            return bookings.length > 0 ? bookings[0] : null;
        } catch (IOException | InterruptedException e) {
            throw new Exception("Ошибка загрузки бронирования: " + e.getMessage(), e);
        }
    }

    public static List<Booking> getBookingsByUserId(int userId) throws Exception {
        try {
            // 🔁 ИСПРАВЛЕНИЕ: используем adminGet вместо get
            Booking[] bookings = SupabaseApiClient.adminGet(TABLE, "user_id", userId, Booking[].class);
            return Arrays.asList(bookings);
        } catch (IOException | InterruptedException e) {
            throw new Exception("Ошибка загрузки бронирований пользователя: " + e.getMessage(), e);
        }
    }

    public static List<Booking> getBookingsByStatus(String status) throws Exception {
        try {
            Booking[] bookings = SupabaseApiClient.get(TABLE, "status", status, Booking[].class);
            return Arrays.asList(bookings);
        } catch (IOException | InterruptedException e) {
            throw new Exception("Ошибка фильтрации бронирований: " + e.getMessage(), e);
        }
    }

    public static List<Booking> getBookingsByDate(LocalDate date) throws Exception {
        try {
            Booking[] bookings = SupabaseApiClient.get(TABLE, "booking_date", date.toString(), Booking[].class);
            return Arrays.asList(bookings);
        } catch (IOException | InterruptedException e) {
            throw new Exception("Ошибка загрузки бронирований на дату: " + e.getMessage(), e);
        }
    }

    public static Booking addBooking(Booking booking) throws Exception {
        try {
            System.out.println("BookingRepository.addBooking: отправка бронирования " + booking);
            Booking[] created = SupabaseApiClient.adminPostForArray(TABLE, booking, Booking.class);
            System.out.println("BookingRepository.addBooking: получено " + created.length + " записей");
            if (created.length > 0) return created[0];
            else throw new Exception("Не удалось создать бронирование");
        } catch (IOException | InterruptedException e) {
            System.err.println("BookingRepository.addBooking: ошибка " + e);
            throw new Exception("Ошибка добавления бронирования: " + e.getMessage(), e);
        }
    }

    public static void updateBookingStatus(int bookingId, String newStatus) throws Exception {
        try {
            Booking patch = new Booking();
            patch.setStatus(newStatus);
            SupabaseApiClient.adminPatch(TABLE, "id", bookingId, patch);
        } catch (IOException | InterruptedException e) {
            throw new Exception("Ошибка обновления статуса бронирования: " + e.getMessage(), e);
        }
    }

    public static void deleteBooking(int bookingId) throws Exception {
        try {
            SupabaseApiClient.adminDelete(TABLE, "id", bookingId);
        } catch (IOException | InterruptedException e) {
            throw new Exception("Ошибка удаления бронирования: " + e.getMessage(), e);
        }
    }
}