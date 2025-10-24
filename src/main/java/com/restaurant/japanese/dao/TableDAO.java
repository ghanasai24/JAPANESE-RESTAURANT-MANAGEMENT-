package com.restaurant.japanese.dao;

import com.restaurant.japanese.model.Table;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TableDAO {

    public void createTable(int tableNumber) {
        String sql = "INSERT INTO restaurant_tables(table_number, status) VALUES(?,?);";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, tableNumber);
            pstmt.setString(2, Table.TableStatus.AVAILABLE.name());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            if (!e.getMessage().contains("SQLITE_CONSTRAINT_UNIQUE")) {
                e.printStackTrace();
            }
        }
    }

    public List<Table> getAllTables() {
        List<Table> tables = new ArrayList<>();
        String sql = "SELECT * FROM restaurant_tables ORDER BY table_number ASC;";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Table table = new Table(
                        rs.getInt("id"),
                        rs.getInt("table_number"),
                        Table.TableStatus.valueOf(rs.getString("status"))
                );
                tables.add(table);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tables;
    }

    public Table getTableById(int tableId) {
        String sql = "SELECT * FROM restaurant_tables WHERE id = ?;";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, tableId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new Table(
                        rs.getInt("id"),
                        rs.getInt("table_number"),
                        Table.TableStatus.valueOf(rs.getString("status"))
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void updateTableStatus(int tableId, Table.TableStatus newStatus) {
        String sql = "UPDATE restaurant_tables SET status = ? WHERE id = ?;";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newStatus.name());
            pstmt.setInt(2, tableId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}