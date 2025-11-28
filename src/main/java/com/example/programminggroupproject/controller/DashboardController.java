package com.example.programminggroupproject.controller;

import com.example.programminggroupproject.model.User;
import com.example.programminggroupproject.session.Session;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;

public class DashboardController {

    @FXML
    private Label welcomeLabel;

    @FXML
    private Button clientButton;

    @FXML
    private Button mechanicButton;

    @FXML
    private Button adminButton;

    private User currentUser;

    public void setUser(User user) {
        this.currentUser = user;

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
        // Clear current user
        Session.clear();

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/programminggroupproject/login-view.fxml")
            );
            Scene loginScene = new Scene(loader.load(), 800, 600);

            String css = getClass()
                    .getResource("/com/example/programminggroupproject/styles.css")
                    .toExternalForm();
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
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/programminggroupproject/client-view.fxml")
            );
            Scene scene = new Scene(loader.load(), 800, 600);

            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Client — Request Service");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleMechanicAction() { /* same as earlier */ }

    @FXML
    private void handleAdminAction() { /* same as earlier */ }
}