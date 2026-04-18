package com.restaurant.luch.models;

import com.google.gson.annotations.SerializedName;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

public class Booking {
    @SerializedName("id")
    private Integer id;

    @SerializedName("user_id")
    private Integer userId;                    // изменено с int на Integer

    @SerializedName("guest_name")
    private String guestName;

    @SerializedName("phone")
    private String phone;

    @SerializedName("booking_date")
    private LocalDate bookingDate;

    @SerializedName("booking_time")
    private LocalTime bookingTime;

    @SerializedName("guests_count")
    private int guestsCount;

    @SerializedName("table_type")
    private String tableType;

    @SerializedName("status")
    private String status;

    @SerializedName("special_requests")
    private String specialRequests;

    @SerializedName("created_date")
    private LocalDateTime createdDate;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getUserId() { return userId; }          // изменён возвращаемый тип
    public void setUserId(Integer userId) { this.userId = userId; }

    public String getGuestName() { return guestName; }
    public void setGuestName(String guestName) { this.guestName = guestName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public LocalDate getBookingDate() { return bookingDate; }
    public void setBookingDate(LocalDate bookingDate) { this.bookingDate = bookingDate; }

    public LocalTime getBookingTime() { return bookingTime; }
    public void setBookingTime(LocalTime bookingTime) { this.bookingTime = bookingTime; }

    public int getGuestsCount() { return guestsCount; }
    public void setGuestsCount(int guestsCount) { this.guestsCount = guestsCount; }

    public String getTableType() { return tableType; }
    public void setTableType(String tableType) { this.tableType = tableType; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getSpecialRequests() { return specialRequests; }
    public void setSpecialRequests(String specialRequests) { this.specialRequests = specialRequests; }

    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }

    public String getStatusDisplay() {
        return switch(status) {
            case "PENDING" -> "⏳ Ожидает подтверждения";
            case "CONFIRMED" -> "✅ Подтверждено";
            case "SEATED" -> "🪑 Гости размещены";
            case "COMPLETED" -> "🏁 Завершено";
            case "CANCELLED" -> "❌ Отменено";
            default -> status;
        };
    }
}