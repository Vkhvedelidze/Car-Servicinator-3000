package com.example.programminggroupproject.controller;

import com.example.programminggroupproject.model.User;
import com.example.programminggroupproject.session.Session;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ClientController {

    @FXML
    private ComboBox<String> vehicleComboBox;

    @FXML
    private ComboBox<String> shopComboBox;

    // Service catalog
    @FXML
    private CheckBox serviceOilCheck;

    @FXML
    private CheckBox serviceTiresCheck;

    @FXML
    private CheckBox serviceBrakesCheck;

    @FXML
    private CheckBox serviceBatteryCheck;

    @FXML
    private CheckBox serviceFluidsCheck;

    @FXML
    private CheckBox serviceEnginePerfCheck;

    @FXML
    private CheckBox serviceCleaningCheck;

    @FXML
    private CheckBox serviceCheckupCheck;

    // Mechanic permissions
    @FXML
    private RadioButton permissionStrictRadio;

    @FXML
    private RadioButton permissionExtraRadio;

    @FXML
    private RadioButton permissionAskRadio;

    @FXML
    private TextField permissionBudgetField;

    @FXML
    private TextArea notesArea;

    @FXML
    private Label messageLabel;

    @FXML
    private Label debugLabel;

    private ToggleGroup permissionGroup;

    @FXML
    public void initialize() {
        // Placeholder data for now
        vehicleComboBox.getItems().addAll(
                "Toyota Corolla - ABC123",
                "Honda Civic - XYZ789",
                "BMW 3 Series - AAA111");
        shopComboBox.getItems().addAll(
                "Downtown Mechanic Shop",
                "Highway Service Center",
                "Premium Auto Care");

        // Group mechanic permissions
        permissionGroup = new ToggleGroup();
        permissionStrictRadio.setToggleGroup(permissionGroup);
        permissionExtraRadio.setToggleGroup(permissionGroup);
        permissionAskRadio.setToggleGroup(permissionGroup);
    }

    @FXML
    private void handleSubmit() {
        String vehicle = vehicleComboBox.getValue();
        String shop = shopComboBox.getValue();

        // Collect chosen services
        List<String> services = new ArrayList<>();
        if (serviceOilCheck.isSelected()) {
            services.add("Oil & Filters");
        }
        if (serviceTiresCheck.isSelected()) {
            services.add("Tires & Alignment");
        }
        if (serviceBrakesCheck.isSelected()) {
            services.add("Brakes");
        }
        if (serviceBatteryCheck.isSelected()) {
            services.add("Battery & Electrical");
        }
        if (serviceFluidsCheck.isSelected()) {
            services.add("Fluids & Leaks");
        }
        if (serviceEnginePerfCheck.isSelected()) {
            services.add("Engine Performance");
        }
        if (serviceCleaningCheck.isSelected()) {
            services.add("Cleaning / Detailing");
        }
        if (serviceCheckupCheck.isSelected()) {
            services.add("General check-up");
        }

        String notes = notesArea.getText().trim();

        // ✅ Validation: vehicle & shop
        if (vehicle == null || shop == null) {
            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setText("Please select a vehicle and a mechanic shop.");
            return;
        }

        // ✅ Validation: at least one service
        if (services.isEmpty()) {
            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setText("Please choose at least one service.");
            return;
        }

        // ✅ Validation: mechanic permissions
        if (permissionGroup.getSelectedToggle() == null) {
            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setText("Please choose the mechanic permissions.");
            return;
        }

        String permissionDescription;

        if (permissionStrictRadio.isSelected()) {
            permissionDescription = "Only the selected services, nothing else.";
        } else if (permissionExtraRadio.isSelected()) {
            String budgetText = permissionBudgetField.getText().trim();
            if (budgetText.isEmpty()) {
                messageLabel.setStyle("-fx-text-fill: red;");
                messageLabel.setText("Please set a € limit for extra work.");
                return;
            }
            double budget;
            try {
                budget = Double.parseDouble(budgetText.replace(",", "."));
            } catch (NumberFormatException e) {
                messageLabel.setStyle("-fx-text-fill: red;");
                messageLabel.setText("Invalid € limit. Use a number like 50 or 100.5");
                return;
            }
            if (budget < 0) {
                messageLabel.setStyle("-fx-text-fill: red;");
                messageLabel.setText("€ limit cannot be negative.");
                return;
            }
            permissionDescription = "Mechanic may do extra work up to " + budget + "€.";
        } else if (permissionAskRadio.isSelected()) {
            permissionDescription = "Mechanic must ask before doing anything else.";
        } else {
            permissionDescription = "Unknown permissions.";
        }

        // Create ServiceRequest
        com.example.programminggroupproject.model.ServiceRequest request = new com.example.programminggroupproject.model.ServiceRequest();
        request.setClientName(
                Session.getCurrentUser() != null ? Session.getCurrentUser().getFullName() : "Unknown Client");
        request.setVehicleInfo(vehicle);
        request.setServiceDescription(String.join(", ", services));
        request.setStatus("Pending");
        request.setCreatedAt(java.time.LocalDateTime.now());

        // Save to DataService
        com.example.programminggroupproject.service.DataService.getInstance().addServiceRequest(request);

        System.out.println("=== New service request saved ===");
        System.out.println("Vehicle: " + vehicle);
        System.out.println("Shop: " + shop);

        messageLabel.setStyle("-fx-text-fill: #28a745;");
        messageLabel.setText("Service request submitted successfully!");

        serviceOilCheck.setSelected(false);
        serviceTiresCheck.setSelected(false);
        serviceBrakesCheck.setSelected(false);
        serviceBatteryCheck.setSelected(false);
        serviceFluidsCheck.setSelected(false);
        serviceEnginePerfCheck.setSelected(false);
        serviceCleaningCheck.setSelected(false);
        serviceCheckupCheck.setSelected(false);
        permissionGroup.selectToggle(null);
        permissionBudgetField.clear();
        notesArea.clear();
    }

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/programminggroupproject/dashboard.fxml"));
            Scene scene = new Scene(loader.load(), 800, 600);

            // Restore current user & role-based dashboard
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