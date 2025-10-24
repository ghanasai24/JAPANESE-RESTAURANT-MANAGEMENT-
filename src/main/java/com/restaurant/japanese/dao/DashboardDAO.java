package com.restaurant.japanese.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

public class DashboardDAO {

    public double getTodaysSales() {
        String sql = "SELECT SUM(final_price) AS total_sales FROM orders WHERE status = 'PAID' AND date(created_at) = date('now', 'localtime');";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                return rs.getDouble("total_sales");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    public Map<String, Integer> getPopularItemsToday() {
        // Use LinkedHashMap to preserve order
        Map<String, Integer> popularItems = new LinkedHashMap<>();
        String sql = "SELECT mi.name, SUM(oi.quantity) AS total_quantity " +
                     "FROM order_items oi " +
                     "JOIN menu_items mi ON oi.menu_item_id = mi.id " +
                     "JOIN orders o ON oi.order_id = o.id " +
                     "WHERE o.status = 'PAID' AND date(o.created_at) = date('now', 'localtime') " +
                     "GROUP BY mi.name " +
                     "ORDER BY total_quantity DESC " +
                     "LIMIT 5;";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                popularItems.put(rs.getString("name"), rs.getInt("total_quantity"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return popularItems;
    }
}