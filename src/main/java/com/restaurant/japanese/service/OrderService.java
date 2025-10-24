package com.restaurant.japanese.service;

import com.restaurant.japanese.dao.InventoryDAO;
import com.restaurant.japanese.dao.MenuItemDAO;
import com.restaurant.japanese.dao.OrderDAO;
import com.restaurant.japanese.exception.InsufficientStockException;
import com.restaurant.japanese.model.Ingredient;
import com.restaurant.japanese.model.MenuItem;
import com.restaurant.japanese.model.Order;
import com.restaurant.japanese.model.OrderItem;

import java.util.Map;

public class OrderService {
    private final InventoryDAO inventoryDAO = new InventoryDAO();
    private final OrderDAO orderDAO = new OrderDAO();
    private final MenuItemDAO menuItemDAO = new MenuItemDAO();

    public boolean canFulfillItem(MenuItem menuItem) {
        if (menuItem.getRecipe() == null) {
            menuItem.setRecipe(menuItemDAO.getRecipeForMenuItem(menuItem.getId()));
        }
        for (Map.Entry<Ingredient, Integer> entry : menuItem.getRecipe().entrySet()) {
            Ingredient ingredientInStock = inventoryDAO.getIngredientById(entry.getKey().getId());
            int requiredQuantity = entry.getValue();
            if (ingredientInStock == null || ingredientInStock.getQuantity() < requiredQuantity) {
                return false;
            }
        }
        return true;
    }
    
    public void confirmOrder(Order order) throws InsufficientStockException {
        // FIXED: Step 1 - Pre-confirmation validation check
        for (OrderItem orderItem : order.getItems()) {
            MenuItem item = orderItem.getMenuItem();
            if (item.getRecipe() == null) {
                item.setRecipe(menuItemDAO.getRecipeForMenuItem(item.getId()));
            }
            for (Map.Entry<Ingredient, Integer> recipeEntry : item.getRecipe().entrySet()) {
                Ingredient requiredIngredient = recipeEntry.getKey();
                int quantityNeeded = recipeEntry.getValue() * orderItem.getQuantity();
                
                Ingredient stockIngredient = inventoryDAO.getIngredientById(requiredIngredient.getId());
                if (stockIngredient.getQuantity() < quantityNeeded) {
                    throw new InsufficientStockException(
                        "Cannot confirm order. Insufficient stock for: " + requiredIngredient.getName()
                    );
                }
            }
        }

        // Step 2 - Consume ingredients (only if validation passes)
        for (OrderItem orderItem : order.getItems()) {
            for (Map.Entry<Ingredient, Integer> recipeEntry : orderItem.getMenuItem().getRecipe().entrySet()) {
                Ingredient ingredient = recipeEntry.getKey();
                int needed = recipeEntry.getValue() * orderItem.getQuantity();
                
                Ingredient currentStock = inventoryDAO.getIngredientById(ingredient.getId());
                int newQuantity = currentStock.getQuantity() - needed;
                inventoryDAO.updateIngredientQuantity(ingredient.getId(), newQuantity);
            }
        }
        
        // Step 3 - Update order status to CONFIRMED
        order.setStatus(Order.OrderStatus.CONFIRMED);
        orderDAO.updateOrder(order);
    }
}