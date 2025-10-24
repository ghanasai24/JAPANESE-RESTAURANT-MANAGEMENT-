package com.restaurant.japanese.dao;

import com.restaurant.japanese.model.User;
import org.mindrot.jbcrypt.BCrypt;
import java.sql.*;

public class UserDAO {

    public void createUser(String username, String plainTextPassword, User.UserRole role) {
        String hashedPassword = BCrypt.hashpw(plainTextPassword, BCrypt.gensalt());
        String sql = "INSERT INTO users(username, password_hash, role) VALUES(?,?,?);";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, hashedPassword);
            pstmt.setString(3, role.name());
            pstmt.executeUpdate();
        } catch (SQLException e) {
             if (!e.getMessage().contains("SQLITE_CONSTRAINT_UNIQUE")) {
                e.printStackTrace();
            }
        }
    }

    public User getUserByUsername(String username) {
        String sql = "SELECT id, username, role FROM users WHERE username = ?;";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        User.UserRole.valueOf(rs.getString("role"))
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public User authenticateUser(String username, String plainTextPassword) {
        String sql = "SELECT password_hash FROM users WHERE username = ?;";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String storedHash = rs.getString("password_hash");
                if (BCrypt.checkpw(plainTextPassword, storedHash)) {
                    return getUserByUsername(username);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}