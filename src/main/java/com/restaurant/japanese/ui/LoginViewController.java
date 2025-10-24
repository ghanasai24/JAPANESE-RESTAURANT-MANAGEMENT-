package com.restaurant.japanese.ui;

import com.restaurant.japanese.Main;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginViewController implements AppAwareController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label loginMessageLabel;

    private Main app;

    @Override
    public void setApp(Main app) {
        this.app = app;
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();

        // The login logic is now in the Main app to keep controllers clean
        app.login(username, password);

        // After attempting login, update the message label
        loginMessageLabel.setText(app.getLoginMessage());
    }
}
