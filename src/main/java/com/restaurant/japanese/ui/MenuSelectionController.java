package com.restaurant.japanese.ui;

import com.restaurant.japanese.Main;
import com.restaurant.japanese.model.MenuItem;
import com.restaurant.japanese.model.Order;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox; // Import CheckBox
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

public class MenuSelectionController implements AppAwareController {

    @FXML private VBox menuItemsVBox;
    @FXML private CheckBox jainCheckBox; // Add FXML field for the checkbox

    private Main app;

    @Override
    public void setApp(Main app) {
        this.app = app;
        populateMenu();
    }
    
    // Add the new handler method
    @FXML
    private void handleJainToggle() {
        Order order = app.getActiveOrder();
        if (order != null) {
            order.getPreferences().setJain(jainCheckBox.isSelected());
            app.getOrderDAO().updateOrder(order);
        }
    }

    private void populateMenu() {
        if (app == null) return;
        
        Order activeOrder = app.getActiveOrder();
        if (activeOrder == null) {
            menuItemsVBox.getChildren().add(new Label("No active order. Please select a table first."));
            jainCheckBox.setVisible(false); // Hide checkbox if no active order
            return;
        }

        // Set the initial state of the checkbox
        jainCheckBox.setVisible(true);
        jainCheckBox.setSelected(activeOrder.getPreferences().isJain());

        menuItemsVBox.getChildren().clear();
        for (MenuItem item : app.getMenuItems()) {
            menuItemsVBox.getChildren().add(createMenuItemCard(item));
        }
    }
    
    // ... createMenuItemCard and handleAddToCart methods remain the same ...
    private AnchorPane createMenuItemCard(MenuItem item) {
        AnchorPane card = new AnchorPane();
        card.getStyleClass().add("menu-item-card");

        ImageView imageView = new ImageView(item.getImage());
        imageView.setFitHeight(80);
        imageView.setFitWidth(80);
        AnchorPane.setLeftAnchor(imageView, 15.0);
        AnchorPane.setTopAnchor(imageView, 15.0);

        Label nameLabel = new Label(item.getName());
        nameLabel.getStyleClass().add("menu-item-name");
        AnchorPane.setLeftAnchor(nameLabel, 130.0);
        AnchorPane.setTopAnchor(nameLabel, 15.0);

        Text descriptionText = new Text(item.getDescription());
        descriptionText.getStyleClass().add("menu-item-description");
        descriptionText.setWrappingWidth(500);
        AnchorPane.setLeftAnchor(descriptionText, 130.0);
        AnchorPane.setTopAnchor(descriptionText, 45.0);

        Label priceLabel = new Label(String.format("₹%.2f", item.getBasePrice()));
        priceLabel.getStyleClass().add("menu-item-price");
        AnchorPane.setRightAnchor(priceLabel, 180.0);
        AnchorPane.setTopAnchor(priceLabel, 30.0);

        Button addButton = new Button("Add to Cart");
        addButton.setOnAction(event -> handleAddToCart(item));
        boolean isAvailable = app.getOrderService().canFulfillItem(item);
        if (!isAvailable) {
            addButton.setDisable(true);
            addButton.setText("Out of Stock");
        }
        AnchorPane.setRightAnchor(addButton, 20.0);
        AnchorPane.setBottomAnchor(addButton, 20.0);

        card.getChildren().addAll(imageView, nameLabel, descriptionText, priceLabel, addButton);
        return card;
    }

    private void handleAddToCart(MenuItem item) {
        Order currentOrder = app.getActiveOrder();
        if (currentOrder != null) {
            app.getOrderDAO().addOrUpdateItemInOrder(currentOrder.getId(), item.getId(), false);
            Order updatedOrder = app.getOrderDAO().getOrderById(currentOrder.getId());
            app.setActiveOrder(updatedOrder);
            System.out.println("Added " + item.getName() + " to order " + currentOrder.getId());
        }
    }
}