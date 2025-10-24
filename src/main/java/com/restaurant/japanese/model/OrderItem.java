package com.restaurant.japanese.model;

public class OrderItem {
    private final MenuItem menuItem;
    private int quantity;
    private boolean customSugarFree;
    private double extraCharge;

    public OrderItem(MenuItem menuItem, int quantity, boolean customSugarFree) {
        this.menuItem = menuItem;
        this.quantity = quantity;
        setCustomSugarFree(customSugarFree);
    }

    public double getPrice() {
        return (menuItem.getBasePrice() + extraCharge) * quantity;
    }

    public MenuItem getMenuItem() { return menuItem; }
    public int getQuantity() { return quantity; }
    public boolean isCustomSugarFree() { return customSugarFree; }
    public void incrementQuantity() { this.quantity++; }
    public void decrementQuantity() { if (this.quantity > 0) this.quantity--; }

    public void setCustomSugarFree(boolean customSugarFree) {
        this.customSugarFree = customSugarFree;
        this.extraCharge = customSugarFree ? 15.0 : 0.0;
    }

    @Override
    public String toString() {
        String base = String.format("%d x %s", quantity, menuItem.getName());
        if (customSugarFree) base += " (Sugar-Free)";
        return base;
    }
}