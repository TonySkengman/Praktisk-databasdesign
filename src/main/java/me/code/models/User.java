package me.code.models;

import java.util.Date;
import java.util.UUID;

public class User {
    private final UUID id;
    private String username;
    private String passwordHash;
    private Date createdAt;

    public User(String username, String password) {
        this.id = UUID.randomUUID();
        this.createdAt = new Date();
        this.username = username;
        this.passwordHash = password;
    }

    public User(UUID id, String username, String password, Date createdAt) {
        this.id = id;
        this.username = username;
        this.passwordHash = password;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String password) {
        this.passwordHash = password;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}