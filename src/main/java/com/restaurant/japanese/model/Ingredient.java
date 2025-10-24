package com.restaurant.japanese.model;

public class Ingredient {

    private final int id;
    private final String name;
    private int quantity;
    private final int minStockLevel;

    public Ingredient(int id, String name, int quantity, int minStockLevel) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
        this.minStockLevel = minStockLevel;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public int getQuantity() { return quantity; }
    public int getMinStockLevel() { return minStockLevel; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    // Add this method for inventory consumption
    public void consume(int amount) {
        this.quantity -= amount;
    }
}
