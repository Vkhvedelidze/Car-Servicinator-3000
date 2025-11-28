package com.example.programminggroupproject.controller;

import com.example.programminggroupproject.model.User;
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
    private void handleLogout() { /* same as earlier */ }

    @FXML
    private void handleClientAction() { /* same as earlier */ }

    @FXML
    private void handleMechanicAction() { /* same as earlier */ }

    @FXML
    private void handleAdminAction() { /* same as earlier */ }
}