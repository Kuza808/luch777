package com.restaurant.luch.models;

import com.google.gson.annotations.SerializedName;
import java.time.LocalDateTime;

public class User {
    @SerializedName("id")
    private Integer id;

    @SerializedName("email")
    private String email;

    @SerializedName("password")
    private String password;                 // переименовано с password_hash

    @SerializedName("name")
    private String name;

    @SerializedName("phone")
    private String phone;

    @SerializedName("address")
    private String address;

    @SerializedName("is_admin")
    private boolean isAdmin;

    @SerializedName("created_at")
    private LocalDateTime createdAt;

    @SerializedName("is_blocked")
    private boolean isBlocked;

    @SerializedName("block_reason")
    private String blockReason;

    @SerializedName("blocked_at")
    private LocalDateTime blockedAt;

    @SerializedName("unblocked_at")
    private LocalDateTime unblockedAt;

    @SerializedName("failed_login_attempts")
    private int failedLoginAttempts;

    @SerializedName("last_login_attempt")
    private LocalDateTime lastLoginAttempt;

    @SerializedName("block_type")
    private String blockType;

    @SerializedName("blocked_by_admin")
    private boolean blockedByAdmin;

    @SerializedName("last_login_ip")
    private String lastLoginIp;

    @SerializedName("last_successful_login")
    private LocalDateTime lastSuccessfulLogin;

    // Геттеры и сеттеры (только основные, остальные можно добавить по необходимости)
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public boolean isAdmin() { return isAdmin; }
    public void setAdmin(boolean admin) { isAdmin = admin; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public boolean isBlocked() { return isBlocked; }
    public void setBlocked(boolean blocked) { isBlocked = blocked; }

    public int getFailedLoginAttempts() { return failedLoginAttempts; }
    public void setFailedLoginAttempts(int failedLoginAttempts) { this.failedLoginAttempts = failedLoginAttempts; }

    // Остальные геттеры/сеттеры можно сгенерировать автоматически в IDE
}