package com.restaurant.japanese.model;

public class ClickableArea {
    private final double x, y, width, height;
    private final Runnable action;

    public ClickableArea(double x, double y, double width, double height, Runnable action) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.action = action;
    }

    public boolean contains(double px, double py) {
        return px >= x && px <= x + width && py >= y && py <= y + height;
    }

    public void executeAction() {
        action.run();
    }
}