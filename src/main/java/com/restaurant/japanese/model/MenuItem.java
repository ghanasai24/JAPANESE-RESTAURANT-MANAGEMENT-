package com.restaurant.japanese.model;

import javafx.scene.image.Image;
import java.util.Map;

public class MenuItem {
    private final int id;
    private final String name;
    private final double basePrice;
    private final String description;
    private final String imagePath;
    private Map<Ingredient, Integer> recipe;

    private double averageRating;
    private int numberOfRatings;
    private transient Image image;

    public MenuItem(int id, String name, double basePrice, String description, String imagePath) {
        this.id = id;
        this.name = name;
        this.basePrice = basePrice;
        this.description = description;
        this.imagePath = imagePath;
    }

    // Getters and Setters
    public int getId() { return id; }
    public String getName() { return name; }
    public double getBasePrice() { return basePrice; }
    public String getDescription() { return description; }
    public String getImagePath() { return imagePath; }
    public Map<Ingredient, Integer> getRecipe() { return recipe; }
    public void setRecipe(Map<Ingredient, Integer> recipe) { this.recipe = recipe; }
    public double getAverageRating() { return averageRating; }
    public void setAverageRating(double averageRating) { this.averageRating = averageRating; }
    public int getNumberOfRatings() { return numberOfRatings; }
    public void setNumberOfRatings(int numberOfRatings) { this.numberOfRatings = numberOfRatings; }
    public Image getImage() { return image; }
    public void setImage(Image image) { this.image = image; }
}