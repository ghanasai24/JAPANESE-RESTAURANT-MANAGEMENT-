package com.restaurant.japanese.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Order {
    public enum OrderStatus { PENDING, CONFIRMED, PREPARING, READY, SERVED, PAID, CANCELLED }
    public enum OrderType { DINE_IN, TAKEAWAY, DELIVERY }
    public enum PaymentMethod { NONE, CASH, CARD, UPI, WALLETS, MULTIPLE }

    private int id;
    private final OrderType orderType;
    private OrderStatus status;
    private PaymentMethod paymentMethod;
    private final List<OrderItem> items;
    private CustomerPreference preferences;
    private Integer tableId;
    private String customerPhoneNumber;
    private final LocalDateTime createdAt;
    private double totalPrice;
    private double gst;
    private double finalPrice;

    public Order(int id, OrderType orderType, OrderStatus status, Integer tableId, String customerPhoneNumber, LocalDateTime createdAt) {
        this.id = id;
        this.orderType = orderType;
        this.status = status;
        this.paymentMethod = PaymentMethod.NONE;
        this.tableId = tableId;
        this.customerPhoneNumber = customerPhoneNumber;
        this.createdAt = createdAt;
        this.items = new ArrayList<>();
        this.preferences = new CustomerPreference();
    }
    
    public void calculateTotals() {
        this.totalPrice = items.stream().mapToDouble(OrderItem::getPrice).sum();
        this.gst = this.totalPrice * 0.05; // 5% GST
        this.finalPrice = this.totalPrice + this.gst;
    }

    public int getTotalItemCount() {
        return items.stream().mapToInt(OrderItem::getQuantity).sum();
    }
    
    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public OrderType getOrderType() { return orderType; }
    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }
    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }
    public List<OrderItem> getItems() { return items; }
    public CustomerPreference getPreferences() { return preferences; }
    public void setPreferences(CustomerPreference preferences) { this.preferences = preferences; }
    public Integer getTableId() { return tableId; }
    public double getTotalPrice() { return totalPrice; }
    public double getGst() { return gst; }
    public double getFinalPrice() { return finalPrice; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public String getCustomerPhoneNumber() { return customerPhoneNumber; }
}