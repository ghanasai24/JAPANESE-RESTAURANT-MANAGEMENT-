package com.restaurant.japanese.dao;

import com.restaurant.japanese.model.Ingredient;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InventoryDAO {

    public Ingredient getIngredientById(int id) {
        String sql = "SELECT * FROM ingredients WHERE id = ?;";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new Ingredient(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getInt("quantity"),
                        rs.getInt("min_stock_level")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public List<Ingredient> getAllIngredients() {
        List<Ingredient> ingredients = new ArrayList<>();
        String sql = "SELECT * FROM ingredients ORDER BY name;";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                ingredients.add(new Ingredient(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getInt("quantity"),
                        rs.getInt("min_stock_level")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ingredients;
    }

    public List<Ingredient> getLowStockIngredients() {
        List<Ingredient> lowStockItems = new ArrayList<>();
        String sql = "SELECT * FROM ingredients WHERE quantity <= min_stock_level ORDER BY name;";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                lowStockItems.add(new Ingredient(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getInt("quantity"),
                        rs.getInt("min_stock_level")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lowStockItems;
    }

    public void updateIngredientQuantity(int ingredientId, int newQuantity) {
        String sql = "UPDATE ingredients SET quantity = ? WHERE id = ?;";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, newQuantity);
            pstmt.setInt(2, ingredientId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public void addOrUpdateIngredient(String name, int quantity, int minStock) {
        String selectSql = "SELECT id FROM ingredients WHERE name = ?;";
        String insertSql = "INSERT INTO ingredients(name, quantity, min_stock_level) VALUES(?,?,?);";
        String updateSql = "UPDATE ingredients SET quantity = quantity + ?, min_stock_level = ? WHERE name = ?;";

        try (Connection conn = DatabaseManager.getConnection()) {
            PreparedStatement selectStmt = conn.prepareStatement(selectSql);
            selectStmt.setString(1, name);
            ResultSet rs = selectStmt.executeQuery();

            if (rs.next()) {
                PreparedStatement updateStmt = conn.prepareStatement(updateSql);
                updateStmt.setInt(1, quantity);
                updateStmt.setInt(2, minStock);
                updateStmt.setString(3, name);
                updateStmt.executeUpdate();
            } else {
                PreparedStatement insertStmt = conn.prepareStatement(insertSql);
                insertStmt.setString(1, name);
                insertStmt.setInt(2, quantity);
                insertStmt.setInt(3, minStock);
                insertStmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}