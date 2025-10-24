package com.restaurant.japanese.ui;

import com.restaurant.japanese.Main;
import com.restaurant.japanese.exception.InsufficientStockException;
import com.restaurant.japanese.model.Order;
import com.restaurant.japanese.model.OrderItem;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class OrderSummaryController implements AppAwareController {

    @FXML private VBox orderItemsVBox;
    @FXML private Label totalPriceLabel;
    @FXML private Button confirmOrderButton;
    @FXML private Button paymentButton;

    private Main app;
    private Order activeOrder;

    @Override
    public void setApp(Main app) {
        this.app = app;
        refreshView();
    }
    
    // THIS IS THE FIX: The @FXML annotation was missing.
    @FXML
    private void handleProceedToPayment() {
        app.getMainViewController().loadView("PaymentView.fxml");
    }

    private void refreshView() {
        if (app.getActiveOrder() == null) {
            orderItemsVBox.getChildren().clear();
            orderItemsVBox.getChildren().add(new Label("No active order."));
            totalPriceLabel.setText("Total: ₹0.00");
            confirmOrderButton.setVisible(false);
            paymentButton.setVisible(false);
            return;
        }
        
        this.activeOrder = app.getOrderDAO().getOrderById(app.getActiveOrder().getId());
        if (this.activeOrder != null) {
            app.setActiveOrder(this.activeOrder);
        } else {
            // The order might have been completed or cancelled
            refreshView(); // Re-call with a null active order
            return;
        }

        orderItemsVBox.getChildren().clear();

        if (activeOrder.getItems().isEmpty()) {
            orderItemsVBox.getChildren().add(new Label("Your order is empty."));
            totalPriceLabel.setText("Total: ₹0.00");
            confirmOrderButton.setDisable(true);
        } else {
            for (OrderItem item : activeOrder.getItems()) {
                orderItemsVBox.getChildren().add(createItemRow(item));
            }
            confirmOrderButton.setDisable(false);
        }
        
        totalPriceLabel.setText(String.format("Total: ₹%.2f", activeOrder.getTotalPrice()));
        
        boolean isPending = activeOrder.getStatus() == Order.OrderStatus.PENDING;
        confirmOrderButton.setVisible(isPending);
        confirmOrderButton.setManaged(isPending);

        boolean isPayable = !isPending && activeOrder.getStatus() != Order.OrderStatus.PAID;
        paymentButton.setVisible(isPayable);
        paymentButton.setManaged(isPayable);
    }
    
    private HBox createItemRow(OrderItem item) {
        HBox row = new HBox(15);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("order-item-row");
        Label nameLabel = new Label(item.getMenuItem().getName());
        nameLabel.setMinWidth(300);
        nameLabel.getStyleClass().add("menu-item-name");
        CheckBox sugarFreeCheckBox = new CheckBox("Sugar-Free (+₹15.00)");
        sugarFreeCheckBox.setSelected(item.isCustomSugarFree());
        sugarFreeCheckBox.setOnAction(e -> handleSugarFreeToggle(item));
        Label priceLabel = new Label(String.format("₹%.2f", item.getPrice()));
        priceLabel.setMinWidth(100);
        HBox quantityControls = createQuantityControls(item);
        Pane spacer = new Pane();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        row.getChildren().addAll(nameLabel, spacer, sugarFreeCheckBox, priceLabel, quantityControls);
        return row;
    }
    
    private void handleSugarFreeToggle(OrderItem item) {
        app.getOrderDAO().toggleSugarFree(activeOrder.getId(), item.getMenuItem().getId(), item.getQuantity(), item.isCustomSugarFree());
        refreshView();
    }

    private HBox createQuantityControls(OrderItem item) {
        HBox controls = new HBox(10);
        controls.setAlignment(Pos.CENTER);
        Button minusButton = new Button("-");
        minusButton.setOnAction(e -> updateQuantity(item, -1));
        Label quantityLabel = new Label(String.valueOf(item.getQuantity()));
        quantityLabel.setMinWidth(25);
        quantityLabel.setAlignment(Pos.CENTER);
        Button plusButton = new Button("+");
        plusButton.setOnAction(e -> updateQuantity(item, 1));
        Button removeButton = new Button("X");
        removeButton.getStyleClass().add("remove-button");
        removeButton.setOnAction(e -> removeItem(item));
        controls.getChildren().addAll(minusButton, quantityLabel, plusButton, removeButton);
        return controls;
    }

    private void updateQuantity(OrderItem item, int delta) {
        int newQuantity = item.getQuantity() + delta;
        if (newQuantity > 0) {
            app.getOrderDAO().updateOrderItem(activeOrder.getId(), item.getMenuItem().getId(), item.isCustomSugarFree(), newQuantity);
        } else {
            removeItem(item);
        }
        refreshView();
    }

    private void removeItem(OrderItem item) {
        app.getOrderDAO().removeOrderItem(activeOrder.getId(), item.getMenuItem().getId(), item.isCustomSugarFree());
        refreshView();
    }

    @FXML
    private void handleConfirmOrder() {
        try {
            app.getOrderService().confirmOrder(activeOrder);
            if (activeOrder.getTableId() != null) {
                app.getTableDAO().updateTableStatus(activeOrder.getTableId(), com.restaurant.japanese.model.Table.TableStatus.OCCUPIED);
            }
            app.setActiveOrder(null);
            app.getMainViewController().loadView("TableView.fxml");
        } catch (InsufficientStockException e) {
            System.err.println("Order Failed: " + e.getMessage());
        }
    }
}