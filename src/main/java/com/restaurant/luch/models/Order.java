package com.restaurant.luch.models;

import com.google.gson.annotations.SerializedName;
import java.time.LocalDateTime;

public class Order {
    @SerializedName("id")
    private Integer id;

    @SerializedName("user_id")
    private Integer userId;                    // изменено с int на Integer

    @SerializedName("total_amount")
    private double totalAmount;

    @SerializedName("status")
    private String status;

    @SerializedName("order_date")
    private LocalDateTime orderDate;

    @SerializedName("order_type")
    private String orderType;

    @SerializedName("special_requests")
    private String specialRequests;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getUserId() { return userId; }          // изменён возвращаемый тип
    public void setUserId(Integer userId) { this.userId = userId; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDateTime orderDate) { this.orderDate = orderDate; }

    public String getOrderType() { return orderType; }
    public void setOrderType(String orderType) { this.orderType = orderType; }

    public String getSpecialRequests() { return specialRequests; }
    public void setSpecialRequests(String specialRequests) { this.specialRequests = specialRequests; }

    public String getStatusDisplay() {
        return switch(status) {
            case "PENDING" -> "⏳ Ожидает подтверждения";
            case "CONFIRMED" -> "✅ Подтвержден";
            case "PREPARING" -> "👨‍🍳 Готовится";
            case "READY" -> "🎉 Готов к выдаче";
            case "COMPLETED" -> "🏁 Завершен";
            case "CANCELLED" -> "❌ Отменен";
            default -> status;
        };
    }
}