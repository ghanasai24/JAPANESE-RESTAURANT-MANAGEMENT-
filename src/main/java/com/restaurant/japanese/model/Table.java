package com.restaurant.japanese.model;

public class Table {
    public enum TableStatus { AVAILABLE, OCCUPIED, NEEDS_CLEANING }

    private final int id;
    private final int tableNumber;
    private TableStatus status;

    public Table(int id, int tableNumber, TableStatus status) {
        this.id = id;
        this.tableNumber = tableNumber;
        this.status = status;
    }

    public int getId() { return id; }
    public int getTableNumber() { return tableNumber; }
    public TableStatus getStatus() { return status; }
    public void setStatus(TableStatus status) { this.status = status; }
}