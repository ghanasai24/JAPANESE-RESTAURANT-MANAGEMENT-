package com.restaurant.japanese.dao;

import com.restaurant.japanese.model.MenuItem;
import com.restaurant.japanese.model.Order;
import com.restaurant.japanese.model.OrderItem;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class OrderDAO {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public Order createOrder(Order.OrderType type, Integer tableId, String customerPhone) {
        String sql = "INSERT INTO orders(order_type, status, table_id, customer_phone, created_at, is_jain, payment_method) VALUES(?,?,?,?,?,?,?);";
        
        try (Connection conn = DatabaseManager.getConnection()) {
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, type.name());
                pstmt.setString(2, Order.OrderStatus.PENDING.name());
                if (tableId != null) {
                    pstmt.setInt(3, tableId);
                } else {
                    pstmt.setNull(3, Types.INTEGER);
                }
                pstmt.setString(4, customerPhone);
                pstmt.setString(5, LocalDateTime.now().format(formatter));
                pstmt.setBoolean(6, false);
                pstmt.setString(7, Order.PaymentMethod.NONE.name());
                
                if (pstmt.executeUpdate() == 0) return null;
            }

            int orderId = -1;
            try (Statement stmt = conn.createStatement()) {
                ResultSet rs = stmt.executeQuery("SELECT last_insert_rowid();");
                if (rs.next()) orderId = rs.getInt(1);
            }

            if (orderId != -1) return getOrderById(orderId);

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public void addOrUpdateItemInOrder(int orderId, int menuItemId, boolean isSugarFree) {
        String selectSql = "SELECT quantity FROM order_items WHERE order_id = ? AND menu_item_id = ? AND is_sugar_free = ?;";
        String insertSql = "INSERT INTO order_items(order_id, menu_item_id, quantity, is_sugar_free) VALUES(?,?,1,?);";
        String updateSql = "UPDATE order_items SET quantity = quantity + 1 WHERE order_id = ? AND menu_item_id = ? AND is_sugar_free = ?;";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement selectStmt = conn.prepareStatement(selectSql)) {
            selectStmt.setInt(1, orderId);
            selectStmt.setInt(2, menuItemId);
            selectStmt.setBoolean(3, isSugarFree);
            ResultSet rs = selectStmt.executeQuery();

            if (rs.next()) {
                try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                    updateStmt.setInt(1, orderId);
                    updateStmt.setInt(2, menuItemId);
                    updateStmt.setBoolean(3, isSugarFree);
                    updateStmt.executeUpdate();
                }
            } else {
                try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                    insertStmt.setInt(1, orderId);
                    insertStmt.setInt(2, menuItemId);
                    insertStmt.setBoolean(3, isSugarFree);
                    insertStmt.executeUpdate();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * NEW: Handles toggling the sugar-free status of an item.
     * This is complex because is_sugar_free is part of the item's identity.
     * It works by decrementing the source item and incrementing/inserting the destination item.
     */
    public void toggleSugarFree(int orderId, int menuItemId, int quantity, boolean currentIsSugarFree) {
        // Decrement the original item
        int originalNewQuantity = quantity - 1;
        if (originalNewQuantity > 0) {
            updateOrderItem(orderId, menuItemId, currentIsSugarFree, originalNewQuantity);
        } else {
            removeOrderItem(orderId, menuItemId, currentIsSugarFree);
        }
        // Add/Increment the toggled item
        addOrUpdateItemInOrder(orderId, menuItemId, !currentIsSugarFree);
    }
    
    // ... (All other methods like getOrderById, updateOrderItem, etc., are unchanged and correct)
    public Order getOrderById(int orderId) {
        String sql = "SELECT * FROM orders WHERE id = ?;";
        Order order = null;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, orderId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                order = new Order(
                        rs.getInt("id"),
                        Order.OrderType.valueOf(rs.getString("order_type")),
                        Order.OrderStatus.valueOf(rs.getString("status")),
                        (Integer) rs.getObject("table_id"),
                        rs.getString("customer_phone"),
                        LocalDateTime.parse(rs.getString("created_at"), formatter)
                );
                order.getPreferences().setJain(rs.getBoolean("is_jain"));
                
                String paymentMethodStr = rs.getString("payment_method");
                if (paymentMethodStr != null) {
                    order.setPaymentMethod(Order.PaymentMethod.valueOf(paymentMethodStr));
                } else {
                    order.setPaymentMethod(Order.PaymentMethod.NONE);
                }

                order.getItems().addAll(getOrderItemsForOrder(order.getId()));
                order.calculateTotals();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return order;
    }
    
    public Order getPendingOrderByTable(int tableId) {
        String sql = "SELECT id FROM orders WHERE table_id = ? AND (status = 'PENDING' OR status = 'CONFIRMED' OR status = 'PREPARING' OR status = 'READY' OR status = 'SERVED');";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, tableId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return getOrderById(rs.getInt("id"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Order> getOrdersByStatus(Order.OrderStatus status) {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT id FROM orders WHERE status = ? ORDER BY created_at ASC;";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, status.name());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                orders.add(getOrderById(rs.getInt("id")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orders;
    }

    public void updateOrderItem(int orderId, int menuItemId, boolean isSugarFree, int newQuantity) {
        if (newQuantity <= 0) {
            removeOrderItem(orderId, menuItemId, isSugarFree);
            return;
        }
        String sql = "UPDATE order_items SET quantity = ? WHERE order_id = ? AND menu_item_id = ? AND is_sugar_free = ?;";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, newQuantity);
            pstmt.setInt(2, orderId);
            pstmt.setInt(3, menuItemId);
            pstmt.setBoolean(4, isSugarFree);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public void removeOrderItem(int orderId, int menuItemId, boolean isSugarFree) {
        String sql = "DELETE FROM order_items WHERE order_id = ? AND menu_item_id = ? AND is_sugar_free = ?;";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, orderId);
            pstmt.setInt(2, menuItemId);
            pstmt.setBoolean(3, isSugarFree);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateOrder(Order order) {
        // CORRECTED SQL: Removed total_price, gst, and final_price which are not in the table.
        String sql = "UPDATE orders SET status = ?, is_jain = ?, payment_method = ? WHERE id = ?;";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            // Set the parameters for the columns that actually exist
            pstmt.setString(1, order.getStatus().name());
            pstmt.setBoolean(2, order.getPreferences().isJain());
            pstmt.setString(3, order.getPaymentMethod().name());
            pstmt.setInt(4, order.getId());
            
            pstmt.executeUpdate();
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private List<OrderItem> getOrderItemsForOrder(int orderId) {
        List<OrderItem> items = new ArrayList<>();
        String sql = "SELECT oi.quantity, oi.is_sugar_free, mi.id AS menu_item_id, mi.name, mi.price, mi.description, mi.image_path " +
                     "FROM order_items oi JOIN menu_items mi ON oi.menu_item_id = mi.id " +
                     "WHERE oi.order_id = ?;";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, orderId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                MenuItem menuItem = new MenuItem(
                        rs.getInt("menu_item_id"),
                        rs.getString("name"),
                        rs.getDouble("price"),
                        rs.getString("description"),
                        rs.getString("image_path")
                );
                OrderItem orderItem = new OrderItem(
                        menuItem,
                        rs.getInt("quantity"),
                        rs.getBoolean("is_sugar_free")
                );
                items.add(orderItem);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }
}