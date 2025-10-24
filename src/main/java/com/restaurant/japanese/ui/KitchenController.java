package com.restaurant.japanese.ui;

import com.restaurant.japanese.Main;
import com.restaurant.japanese.model.Order;
import com.restaurant.japanese.model.OrderItem;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

import java.util.ArrayList;
import java.util.List;

public class KitchenController implements AppAwareController {

    @FXML
    private TilePane ordersTilePane;

    private Main app;

    @Override
    public void setApp(Main app) {
        this.app = app;
        refreshView();
    }

    private void refreshView() {
        if (app == null) return;

        ordersTilePane.getChildren().clear();

        List<Order> confirmedOrders = app.getOrderDAO().getOrdersByStatus(Order.OrderStatus.CONFIRMED);
        List<Order> preparingOrders = app.getOrderDAO().getOrdersByStatus(Order.OrderStatus.PREPARING);

        List<Order> allActiveOrders = new ArrayList<>();
        allActiveOrders.addAll(confirmedOrders);
        allActiveOrders.addAll(preparingOrders);

        if (allActiveOrders.isEmpty()) {
            ordersTilePane.getChildren().add(new Label("No active orders in the kitchen."));
            return;
        }

        for (Order order : allActiveOrders) {
            ordersTilePane.getChildren().add(createOrderCard(order));
        }
    }

    private VBox createOrderCard(Order order) {
        VBox card = new VBox(10);
        card.setPrefWidth(350);
        card.setPadding(new Insets(15));
        card.getStyleClass().add("order-card");

        // Add style based on status
        if (order.getStatus() == Order.OrderStatus.PREPARING) {
            card.getStyleClass().add("order-preparing");
        } else {
            card.getStyleClass().add("order-confirmed");
        }

        // --- Card Header ---
        String titleText = order.getOrderType() == Order.OrderType.DINE_IN
                ? "Table " + app.getTableDAO().getTableById(order.getTableId()).getTableNumber()
                : order.getOrderType().name();
        Label titleLabel = new Label(titleText);
        titleLabel.getStyleClass().add("order-card-title");

        Label statusLabel = new Label(order.getStatus().name());
        statusLabel.getStyleClass().add("order-card-status");

        VBox header = new VBox(titleLabel, statusLabel);

        // --- Items List ---
        VBox itemsBox = new VBox(5);
        for (OrderItem item : order.getItems()) {
            Text itemText = new Text("- " + item.toString());
            itemText.setWrappingWidth(300); // Ensure long item names wrap
            itemsBox.getChildren().add(itemText);
        }

        // --- Action Button ---
        Button actionButton = new Button();
        if (order.getStatus() == Order.OrderStatus.CONFIRMED) {
            actionButton.setText("Start Preparing");
            actionButton.setOnAction(e -> {
                order.setStatus(Order.OrderStatus.PREPARING);
                app.getOrderDAO().updateOrder(order);
                refreshView();
            });
        } else if (order.getStatus() == Order.OrderStatus.PREPARING) {
            actionButton.setText("Mark as Ready");
            actionButton.setOnAction(e -> {
                order.setStatus(Order.OrderStatus.READY);
                app.getOrderDAO().updateOrder(order);
                refreshView();
            });
        }
        actionButton.setMaxWidth(Double.MAX_VALUE);

        card.getChildren().addAll(header, new Separator(), itemsBox, new Separator(), actionButton);
        return card;
    }
}