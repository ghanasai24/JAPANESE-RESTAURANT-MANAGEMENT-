package com.restaurant.japanese.ui;

import com.restaurant.japanese.Main;
import com.restaurant.japanese.model.Ingredient;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;

import java.util.List;

public class InventoryController implements AppAwareController {

    @FXML private TableView<Ingredient> inventoryTableView;
    @FXML private TableColumn<Ingredient, String> nameColumn;
    @FXML private TableColumn<Ingredient, Integer> stockColumn;
    @FXML private TableColumn<Ingredient, Integer> minStockColumn;
    @FXML private TableColumn<Ingredient, Void> actionsColumn;

    private Main app;

    @Override
    public void setApp(Main app) {
        this.app = app;
        initializeTable();
        refreshView();
    }

    private void initializeTable() {
        // Link columns to Ingredient properties
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        stockColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        minStockColumn.setCellValueFactory(new PropertyValueFactory<>("minStockLevel"));

        // Create the custom "Actions" column with buttons
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button restock10Button = new Button("+10");
            private final Button restock50Button = new Button("+50");
            private final HBox pane = new HBox(10, restock10Button, restock50Button);

            {
                pane.setAlignment(Pos.CENTER);
                restock10Button.setOnAction(event -> {
                    Ingredient ingredient = getTableView().getItems().get(getIndex());
                    updateStock(ingredient, 10);
                });
                restock50Button.setOnAction(event -> {
                    Ingredient ingredient = getTableView().getItems().get(getIndex());
                    updateStock(ingredient, 50);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(pane);
                }
            }
        });
    }

    private void updateStock(Ingredient ingredient, int amount) {
        app.getInventoryDAO().updateIngredientQuantity(ingredient.getId(), ingredient.getQuantity() + amount);
        refreshView();
    }

    private void refreshView() {
        List<Ingredient> ingredients = app.getInventoryDAO().getAllIngredients();
        ObservableList<Ingredient> observableIngredients = FXCollections.observableArrayList(ingredients);
        inventoryTableView.setItems(observableIngredients);
        inventoryTableView.refresh();
    }
}