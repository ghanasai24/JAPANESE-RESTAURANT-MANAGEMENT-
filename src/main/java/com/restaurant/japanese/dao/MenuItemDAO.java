package com.restaurant.japanese.dao;

import com.restaurant.japanese.model.Ingredient;
import com.restaurant.japanese.model.MenuItem;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MenuItemDAO {

    public List<MenuItem> getAllMenuItems() {
        List<MenuItem> menuItems = new ArrayList<>();
        String sql = "SELECT * FROM menu_items ORDER BY name;";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                MenuItem item = new MenuItem(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getDouble("price"),
                        rs.getString("description"),
                        rs.getString("image_path")
                );
                item.setRecipe(getRecipeForMenuItem(item.getId()));
                
                Map<String, Double> ratingData = getAggregatedRating(item.getId());
                item.setAverageRating(ratingData.getOrDefault("avg", 0.0));
                item.setNumberOfRatings(ratingData.getOrDefault("count", 0.0).intValue());
                
                menuItems.add(item);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return menuItems;
    }
    
    public int createMenuItem(String name, double price, String description, String imagePath) throws SQLException {
        String sql = "INSERT INTO menu_items(name, price, description, image_path) VALUES(?,?,?,?);";
        
        try (Connection conn = DatabaseManager.getConnection()) {
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, name);
                pstmt.setDouble(2, price);
                pstmt.setString(3, description);
                pstmt.setString(4, imagePath);
                
                int affectedRows = pstmt.executeUpdate();
                if (affectedRows == 0) {
                    throw new SQLException("Creating menu item failed, no rows affected.");
                }
            }

            try (Statement stmt = conn.createStatement()) {
                ResultSet rs = stmt.executeQuery("SELECT last_insert_rowid();");
                if (rs.next()) {
                    return rs.getInt(1);
                } else {
                    throw new SQLException("Creating menu item failed, no ID obtained.");
                }
            }
        }
    }
    
    public Map<Ingredient, Integer> getRecipeForMenuItem(int menuItemId) {
        Map<Ingredient, Integer> recipe = new HashMap<>();
        String sql = "SELECT i.id, i.name, i.quantity, i.min_stock_level, r.quantity_needed " +
                     "FROM recipes r JOIN ingredients i ON r.ingredient_id = i.id " +
                     "WHERE r.menu_item_id = ?;";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, menuItemId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Ingredient ingredient = new Ingredient(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getInt("quantity"),
                        rs.getInt("min_stock_level")
                );
                recipe.put(ingredient, rs.getInt("quantity_needed"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return recipe;
    }
    
    public void addRecipeItem(int menuItemId, String ingredientName, int quantityNeeded) throws SQLException {
        String sql = "INSERT INTO recipes(menu_item_id, ingredient_id, quantity_needed) " +
                     "SELECT ?, id, ? FROM ingredients WHERE name = ?;";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, menuItemId);
            pstmt.setInt(2, quantityNeeded);
            pstmt.setString(3, ingredientName);
            pstmt.executeUpdate();
        }
    }

    public void addRating(int menuItemId, int rating, String comment) {
        String sql = "INSERT INTO dish_ratings(menu_item_id, rating, comment, created_at) VALUES(?,?,?,?);";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, menuItemId);
            pstmt.setInt(2, rating);
            pstmt.setString(3, comment);
            pstmt.setString(4, LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Map<String, Double> getAggregatedRating(int menuItemId) {
        Map<String, Double> ratingData = new HashMap<>();
        ratingData.put("avg", 0.0);
        ratingData.put("count", 0.0);
        String sql = "SELECT AVG(rating) as avg_rating, COUNT(rating) as count_rating FROM dish_ratings WHERE menu_item_id = ?;";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, menuItemId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                ratingData.put("avg", rs.getDouble("avg_rating"));
                ratingData.put("count", (double) rs.getInt("count_rating"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ratingData;
    }
}