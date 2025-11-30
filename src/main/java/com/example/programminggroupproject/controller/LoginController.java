package com.example.programminggroupproject.controller;

import com.example.programminggroupproject.service.AuthService;
import com.example.programminggroupproject.model.User;
import com.example.programminggroupproject.session.Session;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorLabel;

    @FXML
    public void initialize() {
        errorLabel.setText("");
    }

    @FXML
    private void handleLogin() {
        errorLabel.setText("");

        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Please enter username and password");
            return;
        }

        User user = AuthService.authenticate(username, password);

        if (user != null) {
            Session.setCurrentUser(user);
            navigateToDashboard(user);
        } else {
            errorLabel.setText("Invalid username or password");
        }

        System.out.println("Entered username: [" + username + "]");
        System.out.println("Entered password: [" + password + "]");
    }

    private void navigateToDashboard(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/programminggroupproject/dashboard.fxml"));
            Scene dashboardScene = new Scene(loader.load(), 800, 600);

            String css = getClass().getResource("/com/example/programminggroupproject/styles.css").toExternalForm();
            dashboardScene.getStylesheets().add(css);

            DashboardController controller = loader.getController();
            controller.setUser(user);

            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(dashboardScene);
            stage.setTitle("Dashboard - Car Servicinator 3000");
        } catch (IOException e) {
            errorLabel.setText("Error loading dashboard");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleRegister() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/programminggroupproject/register-view.fxml"));
            Scene scene = new Scene(loader.load(), 800, 600);

            // Load CSS if available
            try {
                String css = getClass().getResource("/com/example/programminggroupproject/styles.css").toExternalForm();
                scene.getStylesheets().add(css);
            } catch (Exception e) {
                // Ignore
            }

            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Register - Car Servicinator 3000");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
