package com.restaurant.luch.models;

import com.google.gson.annotations.SerializedName;
import java.time.LocalDateTime;

public class Favorite {
    @SerializedName("id")
    private Integer id;

    @SerializedName("user_id")
    private Integer userId;

    @SerializedName("dish_id")
    private Integer dishId;

    @SerializedName("created_at")
    private LocalDateTime createdAt;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getDishId() {
        return dishId;
    }

    public void setDishId(Integer dishId) {
        this.dishId = dishId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}