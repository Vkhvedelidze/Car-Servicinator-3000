package com.example.programminggroupproject.controller;

import com.example.programminggroupproject.service.AuthService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class RegisterController {

    @FXML
    private TextField fullNameField;

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private ComboBox<String> roleComboBox;

    @FXML
    private Label messageLabel;

    @FXML
    public void initialize() {
        roleComboBox.getItems().addAll("client", "mechanic");
        roleComboBox.setValue("client"); // Default
    }

    @FXML
    private void handleRegister() {
        String fullName = fullNameField.getText();
        String email = usernameField.getText();  // Field name is username but we use it for email
        String password = passwordField.getText();
        String role = roleComboBox.getValue();

        if (fullName.isEmpty() || email.isEmpty() || password.isEmpty() || role == null) {
            messageLabel.setText("Please fill in all fields.");
            messageLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        // Validate email format
        if (!isValidEmail(email)) {
            messageLabel.setText("Please enter a valid email address (e.g., user@example.com)");
            messageLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        // Validate password length
        if (password.length() < 6) {
            messageLabel.setText("Password must be at least 6 characters long.");
            messageLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        boolean success = AuthService.register(email, password, fullName, role);

        if (success) {
            messageLabel.setText("Registration successful! You can now login.");
            messageLabel.setStyle("-fx-text-fill: green;");
            // Optional: Auto-redirect to login or clear fields
            fullNameField.clear();
            usernameField.clear();
            passwordField.clear();
        } else {
            messageLabel.setText("Email already exists or registration failed. Check console for details.");
            messageLabel.setStyle("-fx-text-fill: red;");
        }
    }

    /**
     * Validate email format
     */
    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return email != null && email.matches(emailRegex);
    }

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/programminggroupproject/login-view.fxml"));
            Scene scene = new Scene(loader.load(), 800, 600);

            // Load CSS if available
            try {
                String css = getClass().getResource("/com/example/programminggroupproject/styles.css").toExternalForm();
                scene.getStylesheets().add(css);
            } catch (Exception e) {
                // Ignore if css not found
            }

            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Login - Car Servicinator 3000");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
