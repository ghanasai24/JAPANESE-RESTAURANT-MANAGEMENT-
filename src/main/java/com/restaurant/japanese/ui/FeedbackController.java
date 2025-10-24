package com.restaurant.japanese.ui;

import com.restaurant.japanese.Main;
import com.restaurant.japanese.model.Order;
import com.restaurant.japanese.model.OrderItem;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.HashMap;
import java.util.Map;

public class FeedbackController implements AppAwareController {

    @FXML
    private VBox feedbackItemsVBox;

    private Main app;
    private Order lastPaidOrder;
    private final Map<Integer, Integer> ratings = new HashMap<>();

    @Override
    public void setApp(Main app) {
        this.app = app;
        this.lastPaidOrder = app.getLastPaidOrder();
        populateView();
    }

    private void populateView() {
        feedbackItemsVBox.getChildren().clear();

        if (lastPaidOrder == null || lastPaidOrder.getItems().isEmpty()) {
            // If there's no order, just provide a way to go back
            Label infoLabel = new Label("No order to review.");
            Button backButton = new Button("Back to Tables");
            backButton.setOnAction(e -> handleSkip());
            feedbackItemsVBox.getChildren().addAll(infoLabel, backButton);
            return;
        }

        for (OrderItem item : lastPaidOrder.getItems()) {
            feedbackItemsVBox.getChildren().add(createRatingRow(item));
        }
    }

    private HBox createRatingRow(OrderItem item) {
        HBox row = new HBox(15);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setMaxWidth(600); // Constrain width for centering

        Label itemName = new Label(item.getMenuItem().getName());
        itemName.getStyleClass().add("menu-item-name");

        Pane spacer = new Pane();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Create the 5 stars
        HBox starsBox = new HBox(5);
        int currentRating = ratings.getOrDefault(item.getMenuItem().getId(), 0);
        for (int i = 1; i <= 5; i++) {
            Label star = new Label("★");
            star.getStyleClass().add("star-icon");
            if (i <= currentRating) {
                star.getStyleClass().add("star-icon-filled");
            }

            final int ratingValue = i;
            star.setOnMouseClicked(event -> {
                // Update the rating and redraw the view
                ratings.put(item.getMenuItem().getId(), ratingValue);
                populateView(); // Simple way to refresh the star colors
            });
            starsBox.getChildren().add(star);
        }

        row.getChildren().addAll(itemName, spacer, starsBox);
        return row;
    }

    @FXML
    private void handleSubmit() {
        if (app != null) {
            // Submit ratings to the database
            ratings.forEach((menuItemId, rating) -> {
                if (rating > 0) {
                    app.getMenuItemDAO().addRating(menuItemId, rating, ""); // Empty comment
                }
            });
            finishAndGoBack();
        }
    }

    @FXML
    private void handleSkip() {
        finishAndGoBack();
    }

    private void finishAndGoBack() {
        if (app != null) {
            app.setLastPaidOrder(null); // Clear the last paid order
            app.getMainViewController().loadView("TableView.fxml");
        }
    }
}