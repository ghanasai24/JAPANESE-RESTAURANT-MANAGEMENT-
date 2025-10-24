package com.restaurant.japanese.ui;

import com.restaurant.japanese.Main;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.Map;

public class DashboardController implements AppAwareController {

    @FXML private Label totalSalesLabel;
    @FXML private VBox topItemsVBox;

    private Main app;

    @Override
    public void setApp(Main app) {
        this.app = app;
        populateDashboard();
    }

    private void populateDashboard() {
        // --- Today's Sales ---
        double sales = app.getDashboardDAO().getTodaysSales();
        totalSalesLabel.setText(String.format("₹%.2f", sales));

        // --- Top Selling Items ---
        topItemsVBox.getChildren().clear();
        Map<String, Integer> popularItems = app.getDashboardDAO().getPopularItemsToday();
        if (popularItems.isEmpty()) {
            topItemsVBox.getChildren().add(new Label("No sales data for today yet."));
            return;
        }

        int maxQuantity = popularItems.values().stream().mapToInt(v -> v).max().orElse(1);
        double maxBarWidth = 800; // Max width for the bars

        for (Map.Entry<String, Integer> entry : popularItems.entrySet()) {
            topItemsVBox.getChildren().add(createBarChartRow(entry.getKey(), entry.getValue(), maxQuantity, maxBarWidth));
        }
    }

    private HBox createBarChartRow(String itemName, int quantity, int maxQuantity, double maxBarWidth) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);

        double barWidth = ((double) quantity / maxQuantity) * maxBarWidth;

        // The colored bar
        Region bar = new Region();
        bar.setPrefSize(barWidth, 40);
        bar.getStyleClass().add("chart-bar");

        // The text on top of the bar
        Label nameLabel = new Label(itemName);
        nameLabel.getStyleClass().add("chart-bar-label");

        // Stack the bar and the text
        StackPane barStack = new StackPane(bar, nameLabel);
        barStack.setAlignment(Pos.CENTER_LEFT);

        // The quantity label next to the bar
        Label quantityLabel = new Label(String.valueOf(quantity));
        quantityLabel.getStyleClass().add("chart-quantity-label");

        row.getChildren().addAll(barStack, quantityLabel);
        return row;
    }
}