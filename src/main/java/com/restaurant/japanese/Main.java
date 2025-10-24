package com.restaurant.japanese;

import com.restaurant.japanese.dao.*;
import com.restaurant.japanese.model.Ingredient;
import com.restaurant.japanese.model.MenuItem;
import com.restaurant.japanese.model.Order;
import com.restaurant.japanese.model.User;
import com.restaurant.japanese.service.AuthService;
import com.restaurant.japanese.service.OrderService;
import com.restaurant.japanese.ui.MainViewController;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import java.io.InputStream;
import java.util.List;
import javafx.util.Duration;

public class Main extends Application {

    private Stage primaryStage;
    private MainViewController mainViewController;

    private User loggedInUser;
    private Order activeOrder;
    private Order lastPaidOrder;
    private List<MenuItem> menuItems;
    private String loginMessage;

    private Timeline alertTimeline;

    private final AuthService authService = new AuthService();
    private final OrderService orderService = new OrderService();
    private final TableDAO tableDAO = new TableDAO();
    private final OrderDAO orderDAO = new OrderDAO();
    private final MenuItemDAO menuItemDAO = new MenuItemDAO();
    private final InventoryDAO inventoryDAO = new InventoryDAO();
    private final DashboardDAO dashboardDAO = new DashboardDAO();

    public static void main(String[] args) {
        launch(args);
    }

    private void checkStockLevels() {
        if (loggedInUser != null && loggedInUser.getRole() == User.UserRole.MANAGER) {
            List<Ingredient> lowStockItems = getInventoryDAO().getLowStockIngredients();
            // Pass the list to the main view controller to update the UI
            mainViewController.updateAlerts(lowStockItems);
        } else {
            // If not a manager or not logged in, pass an empty list
            mainViewController.updateAlerts(List.of());
        }
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;
        DatabaseManager.initializeDatabase();
        loadData();

        primaryStage.setTitle("風林火山 Japanese Dining");

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/restaurant/japanese/MainView.fxml"));
        Parent root = loader.load();

        this.mainViewController = loader.getController();
        mainViewController.setApp(this);

        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/com/restaurant/japanese/styles.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.setResizable(true);
        primaryStage.setMinWidth(1000); // Optional: Set a minimum size
        primaryStage.setMinHeight(600);

        showLoginScreen();

        primaryStage.show();
    }


    public void login(String username, String password) {
        User user = getAuthService().login(username, password);
        if (user != null) {
            setLoggedInUser(user);
            setLoginMessage(""); // Clear any previous error message
            showMainApplication();
            if (alertTimeline == null) {
                alertTimeline = new Timeline(new KeyFrame(Duration.seconds(10), e -> checkStockLevels()));
                alertTimeline.setCycleCount(Timeline.INDEFINITE);
            }
        } else {
            setLoginMessage("Invalid username or password.");
        }
        alertTimeline.play();
    }

    public void logout() {
        loggedInUser = null;
        activeOrder = null;
        lastPaidOrder = null;
        if (alertTimeline != null) {
            alertTimeline.stop(); // Stop the checker
        }
        mainViewController.updateAlerts(List.of()); // Clear alerts
        showLoginScreen();
    }

    public void showLoginScreen() {
        mainViewController.loadView("LoginView.fxml");
    }

    public void showMainApplication() {
        mainViewController.loadView("TableView.fxml");
        mainViewController.updateLoginStatus(loggedInUser);
    }

    private void loadData() {
        menuItems = menuItemDAO.getAllMenuItems();
        for(MenuItem item : menuItems) {
            if(item.getImagePath() != null && !item.getImagePath().isEmpty()) {
                try (InputStream is = getClass().getClassLoader().getResourceAsStream(item.getImagePath())) {
                    if (is != null) {
                        item.setImage(new Image(is));
                    }
                } catch (Exception e) {
                    System.err.println("Could not load image: " + item.getImagePath());
                }
            }
        }
    }

    // --- GETTERS AND SETTERS ---
    public AuthService getAuthService() { return authService; }
    public OrderService getOrderService() { return orderService; }
    public TableDAO getTableDAO() { return tableDAO; }
    public OrderDAO getOrderDAO() { return orderDAO; }
    public MenuItemDAO getMenuItemDAO() { return menuItemDAO; }
    public InventoryDAO getInventoryDAO() { return inventoryDAO; }
    public DashboardDAO getDashboardDAO() { return dashboardDAO; }
    public User getLoggedInUser() { return loggedInUser; }
    public void setLoggedInUser(User user) { this.loggedInUser = user; }
    public Order getActiveOrder() { return activeOrder; }
    public void setActiveOrder(Order order) {
    this.activeOrder = order;
    mainViewController.updateCartCount(order); // ADD THIS LINE
} 
    public Order getLastPaidOrder() { return lastPaidOrder; }
    public void setLastPaidOrder(Order order) { this.lastPaidOrder = order; }
    public List<MenuItem> getMenuItems() { return menuItems; }
    public String getLoginMessage() { return loginMessage; }
    public void setLoginMessage(String msg) { this.loginMessage = msg; }
    public MainViewController getMainViewController() { return mainViewController; }
}
