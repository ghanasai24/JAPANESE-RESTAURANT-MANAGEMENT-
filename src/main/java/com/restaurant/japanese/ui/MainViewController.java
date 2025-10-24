package com.restaurant.japanese.ui;

import com.restaurant.japanese.Main;
import com.restaurant.japanese.model.Ingredient;
import com.restaurant.japanese.model.Order;
import com.restaurant.japanese.model.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox; // Import VBox
import javafx.scene.shape.Circle;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainViewController {

    @FXML private AnchorPane contentArea;
    @FXML private Label loggedInUserLabel;
    // Add these fields at the top with your other @FXML fields
@FXML private StackPane alertButtonPane;
@FXML private Circle alertDot;

private List<Ingredient> lowStockItems = new ArrayList<>(); // Store the items

// Add this handler method
@FXML
private void handleAlerts() {
    if (lowStockItems.isEmpty()) {
        // Show a standard JavaFX Alert
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Stock Status");
        alert.setHeaderText("All ingredients are adequately stocked.");
        alert.showAndWait();
    } else {
        StringBuilder sb = new StringBuilder("The following ingredients are low on stock:\n");
        lowStockItems.forEach(item -> sb.append(String.format("\n- %s (Stock: %d, Min: %d)", item.getName(), item.getQuantity(), item.getMinStockLevel())));
        
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Stock Warning");
        alert.setHeaderText("Low Stock Alert!");
        alert.setContentText(sb.toString());
        alert.showAndWait();
    }
}

// Add this public method
public void updateAlerts(List<Ingredient> lowStockItems) {
    this.lowStockItems = lowStockItems;
    boolean isManager = app.getLoggedInUser() != null && app.getLoggedInUser().getRole() == User.UserRole.MANAGER;
    
    alertButtonPane.setVisible(isManager); // Only managers see the button
    alertDot.setVisible(isManager && !lowStockItems.isEmpty()); // Only show dot if there are alerts
}
    
    // --- NEWLY ADDED for Sidebar Injection ---
    @FXML private VBox sidebar; // This refers to the <fx:include fx:id="sidebar" ...>
    @FXML private SidebarController sidebarController; // JavaFX injects the controller of the included FXML

    // --- NEWLY ADDED for Cart Button ---
    @FXML private Button cartButton;

    private Main app;

    public void setApp(Main app) {
        this.app = app;
        // This is the new, robust way to connect the sidebar
        if (sidebarController != null) {
            sidebarController.setApp(this.app);
        }
    }

    // This method is now more robust and handles missing files
    public void loadView(String fxmlFile) {
        try {
            java.net.URL fxmlUrl = getClass().getResource("/com/restaurant/japanese/" + fxmlFile);

            if (fxmlUrl == null) {
                System.err.println("Cannot find FXML file: " + fxmlFile);
                Label errorLabel = new Label("Error: View not found (" + fxmlFile + ")");
                errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 18px;");
                contentArea.getChildren().setAll(errorLabel);
                AnchorPane.setTopAnchor(errorLabel, 20.0);
                AnchorPane.setLeftAnchor(errorLabel, 20.0);
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent view = loader.load();

            Object controller = loader.getController();
            if (controller instanceof AppAwareController) {
                ((AppAwareController) controller).setApp(app);
            }
            contentArea.getChildren().setAll(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    // --- NEWLY ADDED: Method to handle cart button click ---
    @FXML
    private void handleCartButton() {
        if (app.getActiveOrder() != null) {
            loadView("OrderSummary.fxml");
        } else {
            // In the future, we can show an alert here
            System.out.println("No active order to show summary for.");
        }
    }

    public void updateLoginStatus(User user) {
        if (user != null) {
            loggedInUserLabel.setText("User: " + user.getUsername());
            sidebar.setVisible(true); // Show sidebar when logged in
        } else {
            loggedInUserLabel.setText("Not Logged In");
            sidebar.setVisible(false); // Hide sidebar when logged out
        }
        updateCartCount(null); // Reset cart on login/logout
    }

    // --- NEWLY ADDED: Method to update the cart button's text ---
    public void updateCartCount(Order order) {
        if (order != null && order.getTotalItemCount() > 0) {
            cartButton.setText(String.format("Cart (%d)", order.getTotalItemCount()));
            cartButton.setVisible(true);
        } else {
            cartButton.setText("Cart (0)");
            cartButton.setVisible(false); // Hide cart button if empty
        }
    }
}