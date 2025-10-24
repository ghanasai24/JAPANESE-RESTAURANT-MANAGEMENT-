package com.restaurant.japanese.dao;

import com.restaurant.japanese.model.User;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.mindrot.jbcrypt.BCrypt;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {

    private static final String DB_URL = "jdbc:sqlite:restaurant.db";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    public static void initializeDatabase() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            // User and Staff Management
            stmt.execute("CREATE TABLE IF NOT EXISTS users (id INTEGER PRIMARY KEY, username TEXT UNIQUE NOT NULL, password_hash TEXT NOT NULL, role TEXT NOT NULL);");
            
            // Inventory
            stmt.execute("CREATE TABLE IF NOT EXISTS ingredients (id INTEGER PRIMARY KEY, name TEXT UNIQUE NOT NULL, quantity INTEGER NOT NULL, min_stock_level INTEGER NOT NULL);");
            
            // Menu, Recipes, and Ratings
            stmt.execute("CREATE TABLE IF NOT EXISTS menu_items (id INTEGER PRIMARY KEY, name TEXT UNIQUE NOT NULL, price REAL NOT NULL, description TEXT NOT NULL, image_path TEXT);");
            stmt.execute("CREATE TABLE IF NOT EXISTS recipes (menu_item_id INTEGER NOT NULL, ingredient_id INTEGER NOT NULL, quantity_needed INTEGER NOT NULL, FOREIGN KEY (menu_item_id) REFERENCES menu_items(id), FOREIGN KEY (ingredient_id) REFERENCES ingredients(id), PRIMARY KEY (menu_item_id, ingredient_id));");
            stmt.execute("CREATE TABLE IF NOT EXISTS dish_ratings (id INTEGER PRIMARY KEY, menu_item_id INTEGER NOT NULL, rating INTEGER NOT NULL, comment TEXT, created_at TEXT NOT NULL, FOREIGN KEY (menu_item_id) REFERENCES menu_items(id));");

            // Restaurant Operations
            stmt.execute("CREATE TABLE IF NOT EXISTS restaurant_tables (id INTEGER PRIMARY KEY, table_number INTEGER UNIQUE NOT NULL, status TEXT NOT NULL);");
            stmt.execute("CREATE TABLE IF NOT EXISTS orders (id INTEGER PRIMARY KEY, order_type TEXT NOT NULL, status TEXT NOT NULL, table_id INTEGER, customer_phone TEXT, created_at TEXT NOT NULL, total_price REAL, gst REAL, final_price REAL, is_jain BOOLEAN, payment_method TEXT, FOREIGN KEY (table_id) REFERENCES restaurant_tables(id));");
            stmt.execute("CREATE TABLE IF NOT EXISTS order_items (id INTEGER PRIMARY KEY, order_id INTEGER NOT NULL, menu_item_id INTEGER NOT NULL, quantity INTEGER NOT NULL, is_sugar_free BOOLEAN, FOREIGN KEY (order_id) REFERENCES orders(id), FOREIGN KEY (menu_item_id) REFERENCES menu_items(id));");

            seedUsers();
            seedTables();
            seedIngredientsAndMenu();

            System.out.println("Database initialized successfully.");
        } catch (Exception e) {
            System.err.println("Database initialization failed.");
            e.printStackTrace();
        }
    }

    private static void seedUsers() {
        UserDAO userDAO = new UserDAO();
        if (userDAO.getUserByUsername("manager") == null) {
            userDAO.createUser("manager", "manager123", User.UserRole.MANAGER);
        }
        if (userDAO.getUserByUsername("staff") == null) {
            userDAO.createUser("staff", "staff123", User.UserRole.STAFF);
        }
    }

    private static void seedTables() {
        TableDAO tableDAO = new TableDAO();
        if (tableDAO.getAllTables().isEmpty()) {
            for (int i = 1; i <= 10; i++) {
                tableDAO.createTable(i);
            }
        }
    }

    private static void seedIngredientsAndMenu() {
        try {
            MenuItemDAO menuItemDAO = new MenuItemDAO();
            if (!menuItemDAO.getAllMenuItems().isEmpty()) return;

            InventoryDAO inventoryDAO = new InventoryDAO();
            inventoryDAO.addOrUpdateIngredient("Tuna", 50, 10);
            inventoryDAO.addOrUpdateIngredient("Sushi Rice", 200, 50);
            inventoryDAO.addOrUpdateIngredient("Salmon", 50, 10);
            inventoryDAO.addOrUpdateIngredient("Avocado", 40, 15);
            inventoryDAO.addOrUpdateIngredient("Nori", 100, 20);
            inventoryDAO.addOrUpdateIngredient("Udon Noodles", 100, 20);
            inventoryDAO.addOrUpdateIngredient("Onion", 80, 25);
            inventoryDAO.addOrUpdateIngredient("Prawn", 60, 20);
            inventoryDAO.addOrUpdateIngredient("Tempura Batter", 100, 20);
            inventoryDAO.addOrUpdateIngredient("Cucumber", 100, 20);
            inventoryDAO.addOrUpdateIngredient("Garlic", 100, 20);

            JSONParser parser = new JSONParser();
            InputStream is = DatabaseManager.class.getClassLoader().getResourceAsStream("menu.json");
            if (is == null) {
                System.err.println("menu.json not found in resources!");
                return;
            }
            JSONArray menuArray = (JSONArray) parser.parse(new InputStreamReader(is));

            for (Object obj : menuArray) {
                JSONObject menuItemJson = (JSONObject) obj;
                String name = (String) menuItemJson.get("name");
                double price = ((Number) menuItemJson.get("price")).doubleValue();
                String description = (String) menuItemJson.get("description");
                String imagePath = (String) menuItemJson.get("image_path");
                
                int menuItemId = menuItemDAO.createMenuItem(name, price, description, imagePath);

                JSONObject recipeJson = (JSONObject) menuItemJson.get("recipe");
                for (Object key : recipeJson.keySet()) {
                    String ingredientName = (String) key;
                    int quantityNeeded = ((Number) recipeJson.get(key)).intValue();
                    menuItemDAO.addRecipeItem(menuItemId, ingredientName, quantityNeeded);
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to seed menu and ingredients.");
            e.printStackTrace();
        }
    }
}