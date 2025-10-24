package com.restaurant.japanese.model;

public class User {
    public enum UserRole { STAFF, MANAGER }

    private final int id;
    private final String username;
    private final UserRole role;

    public User(int id, String username, UserRole role) {
        this.id = id;
        this.username = username;
        this.role = role;
    }

    public int getId() { return id; }
    public String getUsername() { return username; }
    public UserRole getRole() { return role; }
}