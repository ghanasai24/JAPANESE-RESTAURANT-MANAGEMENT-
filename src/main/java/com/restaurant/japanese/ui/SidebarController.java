package com.restaurant.japanese.ui;

import com.restaurant.japanese.Main;
import javafx.fxml.FXML;

// Add "implements AppAwareController" if it's missing
public class SidebarController implements AppAwareController {

    private Main app;

    // Change this method from private/default to public
    @Override
    public void setApp(Main app) {
        this.app = app;
    }

    @FXML
    private void handleTableView() {
        app.getMainViewController().loadView("TableView.fxml");
    }

    @FXML
    private void handleKitchenView() {
        app.getMainViewController().loadView("KitchenView.fxml");
    }

    @FXML
    private void handleDashboard() {
        app.getMainViewController().loadView("DashboardView.fxml");
    }

    @FXML
    private void handleInventory() {
        app.getMainViewController().loadView("InventoryView.fxml");
    }

    @FXML
    private void handleLogout() {
        app.logout();
    }
}