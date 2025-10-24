package com.restaurant.japanese.ui;

import com.restaurant.japanese.Main;
import com.restaurant.japanese.model.Order;
import com.restaurant.japanese.model.Table;
import com.restaurant.japanese.model.User;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TableViewController implements AppAwareController {

    @FXML
    private TilePane tableTilePane;

    private Main app;

    @Override
    public void setApp(Main app) {
        this.app = app;
        refreshView();
    }

    private void refreshView() {
        if (app == null) return;

        tableTilePane.getChildren().clear();

        List<Table> tables = app.getTableDAO().getAllTables();
        List<Order> readyOrders = app.getOrderDAO().getOrdersByStatus(Order.OrderStatus.READY);
        
        // --- DEBUG LINE ADDED ---
        System.out.println("DEBUG: refreshView() called. Found " + readyOrders.size() + " ready orders.");

        Map<Integer, Order> tableIdToReadyOrderMap = readyOrders.stream()
                .filter(o -> o.getTableId() != null)
                .collect(Collectors.toMap(Order::getTableId, o -> o));

        for (Table table : tables) {
            boolean isFoodReady = tableIdToReadyOrderMap.containsKey(table.getId());
            VBox tableCard = createTableCard(table, isFoodReady);
            tableTilePane.getChildren().add(tableCard);
        }
    }

    private VBox createTableCard(Table table, boolean isFoodReady) {
        VBox card = new VBox(10);
        card.setPrefSize(140, 140);
        card.setAlignment(Pos.CENTER);
        card.getStyleClass().add("table-card");

        if (isFoodReady) {
            card.getStyleClass().add("table-food-ready");
        } else {
            switch (table.getStatus()) {
                case OCCUPIED -> card.getStyleClass().add("table-occupied");
                case NEEDS_CLEANING -> card.getStyleClass().add("table-needs-cleaning");
                default -> card.getStyleClass().add("table-available");
            }
        }

        Label tableNumberLabel = new Label(String.valueOf(table.getTableNumber()));
        tableNumberLabel.getStyleClass().add("table-number-label");

        String statusText = isFoodReady ? "FOOD READY" : table.getStatus().name().replace("_", " ");
        Label statusLabel = new Label(statusText);
        statusLabel.getStyleClass().add("table-status-label");

        card.getChildren().addAll(tableNumberLabel, statusLabel);
        card.setOnMouseClicked(event -> handleTableClick(table, isFoodReady));

        return card;
    }

    private void handleTableClick(Table table, boolean isFoodReady) {
        // --- DEBUG LINE ADDED ---
        System.out.println("DEBUG: handleTableClick started for table " + table.getId() + ". isFoodReady=" + isFoodReady);

        if (isFoodReady) {
            Order readyOrder = app.getOrderDAO().getPendingOrderByTable(table.getId());
            if (readyOrder != null) {
                // --- DEBUG LINE ADDED ---
                System.out.println("DEBUG: Updating order " + readyOrder.getId() + " status to SERVED.");

                readyOrder.setStatus(Order.OrderStatus.SERVED);
                app.getOrderDAO().updateOrder(readyOrder);
                
                // --- DEBUG LINE ADDED ---
                System.out.println("DEBUG: Calling refreshView().");
                refreshView(); 
            }
            return;
        }

        switch (table.getStatus()) {
            case AVAILABLE:
                Order newOrder = app.getOrderDAO().createOrder(Order.OrderType.DINE_IN, table.getId(), null);
                if (newOrder != null) {
                    app.setActiveOrder(newOrder);
                    app.getMainViewController().loadView("MenuSelection.fxml");
                }
                break;
            case OCCUPIED:
                Order existingOrder = app.getOrderDAO().getPendingOrderByTable(table.getId());
                if (existingOrder != null) {
                    app.setActiveOrder(existingOrder);
                    app.getMainViewController().loadView("OrderSummary.fxml");
                }
                break;
            case NEEDS_CLEANING:
                if (app.getLoggedInUser().getRole() == User.UserRole.MANAGER) {
                    app.getTableDAO().updateTableStatus(table.getId(), Table.TableStatus.AVAILABLE);
                    refreshView();
                } else {
                    System.out.println("This table needs to be cleaned by a manager.");
                }
                break;
        }
    }
}