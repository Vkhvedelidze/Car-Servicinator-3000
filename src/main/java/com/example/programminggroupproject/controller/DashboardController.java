package com.example.programminggroupproject.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.io.IOException;

import com.example.programminggroupproject.model.User;

public class DashboardController {

    @FXML
    private Label welcomeLabel;
    @FXML
    private Button clientButton;
    @FXML
    private Button mechanicButton;
    @FXML
    private Button adminButton;

    public void setUser(User user) {
        if (user != null) {
            welcomeLabel.setText("Welcome, " + user.getFullName() + " (" + user.getRole() + ")");
            clientButton.setVisible(false);
            mechanicButton.setVisible(false);
            adminButton.setVisible(false);

            switch (user.getRole()) {
                case "client":
                    clientButton.setVisible(true);
                    break;
                case "mechanic":
                    mechanicButton.setVisible(true);
                    break;
                case "admin":
                    adminButton.setVisible(true);
                    break;
            }
        }
    }


    @FXML
    private void handleLogout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/programminggroupproject/login-view.fxml"));
            Scene loginScene = new Scene(loader.load(), 800, 600);

            String css = getClass().getResource("/com/example/programminggroupproject/styles.css").toExternalForm();
            loginScene.getStylesheets().add(css);

            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            stage.setScene(loginScene);
            stage.setTitle("Login - Car Servicinator 3000");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleClientAction() {
        System.out.println("Client action triggered");
    }

    @FXML
    private void handleMechanicAction() {
        System.out.println("Mechanic action triggered");
    }

    @FXML
    private void handleAdminAction() {
        System.out.println("Admin action triggered");
    }
}
