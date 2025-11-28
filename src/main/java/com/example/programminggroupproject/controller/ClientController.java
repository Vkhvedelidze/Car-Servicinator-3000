package com.example.programminggroupproject.controller;

import com.example.programminggroupproject.model.User;
import com.example.programminggroupproject.session.Session;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;

public class ClientController {

    @FXML
    private Label debugLabel;

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/programminggroupproject/dashboard.fxml")
            );
            Scene scene = new Scene(loader.load(), 800, 600);

            // 🔥 Get the controller and restore the current user/role
            DashboardController controller = loader.getController();
            User currentUser = Session.getCurrentUser();
            if (currentUser != null) {
                controller.setUser(currentUser);
            }

            Stage stage = (Stage) debugLabel.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Dashboard - Car Servicinator 3000");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}