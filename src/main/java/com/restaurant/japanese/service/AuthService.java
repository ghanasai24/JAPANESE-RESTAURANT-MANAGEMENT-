package com.restaurant.japanese.service;

import com.restaurant.japanese.dao.UserDAO;
import com.restaurant.japanese.model.User;

public class AuthService {
    private final UserDAO userDAO = new UserDAO();

    public User login(String username, String password) {
        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            return null;
        }
        return userDAO.authenticateUser(username, password);
    }
}