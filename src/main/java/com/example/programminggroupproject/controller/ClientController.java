package com.example.programminggroupproject.controller;

import com.example.programminggroupproject.model.ServiceRequest;
import com.example.programminggroupproject.model.User;
import com.example.programminggroupproject.model.Vehicle;
import com.example.programminggroupproject.model.MechanicShop;
import com.example.programminggroupproject.model.Service;
import com.example.programminggroupproject.service.ServiceRequestService;
import com.example.programminggroupproject.service.VehicleService;
import com.example.programminggroupproject.service.MechanicShopService;
import com.example.programminggroupproject.service.MechanicalService;
import com.example.programminggroupproject.session.Session;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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

    @FXML
    private Label totalPriceLabel;

    private ToggleGroup permissionGroup;

    private final ServiceRequestService serviceRequestService = ServiceRequestService.getInstance();
    private final VehicleService vehicleService = VehicleService.getInstance();
    private final MechanicShopService shopService = MechanicShopService.getInstance();
    private final MechanicalService mechanicalService = MechanicalService.getInstance();

    private List<Vehicle> userVehicles;
    private List<MechanicShop> availableShops;
    private Map<CheckBox, BigDecimal> servicePrices = new HashMap<>();

    @FXML
    public void initialize() {
        // Group mechanic permissions
        permissionGroup = new ToggleGroup();
        permissionStrictRadio.setToggleGroup(permissionGroup);
        permissionExtraRadio.setToggleGroup(permissionGroup);
        permissionAskRadio.setToggleGroup(permissionGroup);

        // Load data from Supabase
        loadVehiclesAndShops();

        // Load service prices and add listeners
        loadServicePrices();
        setupPriceListeners();
    }

    private void loadVehiclesAndShops() {
        User currentUser = Session.getCurrentUser();
        if (currentUser == null) {
            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setText("No user logged in. Please log in again.");
            return;
        }

        try {
            // Load user's vehicles
            userVehicles = vehicleService.getByClientId(currentUser.getId());
            vehicleComboBox.getItems().clear();
            for (Vehicle vehicle : userVehicles) {
                String displayText = vehicle.getMake() + " " + vehicle.getModel() +
                        " - " + vehicle.getLicensePlate();
                vehicleComboBox.getItems().add(displayText);
            }

            if (userVehicles.isEmpty()) {
                vehicleComboBox.setPromptText("No vehicles registered");
                System.out.println("No vehicles registered");
            }

            // Load available mechanic shops
            availableShops = shopService.getAllOrderedByName();
            shopComboBox.getItems().clear();
            for (MechanicShop shop : availableShops) {
                shopComboBox.getItems().add(shop.getName());
            }

            if (availableShops.isEmpty()) {
                shopComboBox.setPromptText("No shops available");
            }

        } catch (Exception e) {
            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setText("Error loading data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadServicePrices() {
        try {
            List<Service> services = mechanicalService.getAll();
            for (Service service : services) {
                String name = service.getName().toLowerCase();
                BigDecimal price = service.getBasePrice();

                if (name.contains("oil")) {
                    serviceOilCheck.setText("🔧 Oil & Filters - €" + price);
                    servicePrices.put(serviceOilCheck, price);
                } else if (name.contains("tire") || name.contains("alignment")) {
                    serviceTiresCheck.setText("🛞 Tires & Alignment - €" + price);
                    servicePrices.put(serviceTiresCheck, price);
                } else if (name.contains("brake")) {
                    serviceBrakesCheck.setText("🛑 Brakes - €" + price);
                    servicePrices.put(serviceBrakesCheck, price);
                } else if (name.contains("battery") || name.contains("electric")) {
                    serviceBatteryCheck.setText("🔋 Battery & Electrical - €" + price);
                    servicePrices.put(serviceBatteryCheck, price);
                } else if (name.contains("fluid") || name.contains("leak")) {
                    serviceFluidsCheck.setText("💧 Fluids & Leaks - €" + price);
                    servicePrices.put(serviceFluidsCheck, price);
                } else if (name.contains("engine") || name.contains("performance")) {
                    serviceEnginePerfCheck.setText("⚙️ Engine Performance - €" + price);
                    servicePrices.put(serviceEnginePerfCheck, price);
                } else if (name.contains("clean") || name.contains("detail")) {
                    serviceCleaningCheck.setText("🧽 Cleaning / Detailing - €" + price);
                    servicePrices.put(serviceCleaningCheck, price);
                } else if (name.contains("check") || name.contains("inspection") || name.contains("general")) {
                    serviceCheckupCheck.setText("🔍 General Check-up - €" + price);
                    servicePrices.put(serviceCheckupCheck, price);
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading service prices: " + e.getMessage());
        }
    }

    private void setupPriceListeners() {
        CheckBox[] checkboxes = {serviceOilCheck, serviceTiresCheck, serviceBrakesCheck,
                serviceBatteryCheck, serviceFluidsCheck, serviceEnginePerfCheck,
                serviceCleaningCheck, serviceCheckupCheck};

        for (CheckBox cb : checkboxes) {
            cb.selectedProperty().addListener((obs, oldVal, newVal) -> updateTotalPrice());
        }
    }

    private void updateTotalPrice() {
        BigDecimal total = BigDecimal.ZERO;
        for (Map.Entry<CheckBox, BigDecimal> entry : servicePrices.entrySet()) {
            if (entry.getKey().isSelected()) {
                total = total.add(entry.getValue());
            }
        }
        totalPriceLabel.setText("Estimated Total: €" + total.setScale(2));
    }

    @FXML
    private void handleSubmit() {
        String vehicleSelection = vehicleComboBox.getValue();
        String shopSelection = shopComboBox.getValue();

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
        if (vehicleSelection == null || shopSelection == null) {
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
        BigDecimal budget = null;

        if (permissionStrictRadio.isSelected()) {
            permissionDescription = "Only the selected services, nothing else.";
        } else if (permissionExtraRadio.isSelected()) {
            String budgetText = permissionBudgetField.getText().trim();
            if (budgetText.isEmpty()) {
                messageLabel.setStyle("-fx-text-fill: red;");
                messageLabel.setText("Please set a € limit for extra work.");
                return;
            }
            try {
                budget = new BigDecimal(budgetText.replace(",", "."));
            } catch (NumberFormatException e) {
                messageLabel.setStyle("-fx-text-fill: red;");
                messageLabel.setText("Invalid € limit. Use a number like 50 or 100.5");
                return;
            }
            if (budget.compareTo(BigDecimal.ZERO) < 0) {
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

        // Get the selected vehicle and shop IDs
        int vehicleIndex = vehicleComboBox.getSelectionModel().getSelectedIndex();
        int shopIndex = shopComboBox.getSelectionModel().getSelectedIndex();

        if (vehicleIndex < 0 || shopIndex < 0) {
            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setText("Invalid selection. Please try again.");
            return;
        }

        UUID vehicleId = userVehicles.get(vehicleIndex).getId();
        UUID shopId = availableShops.get(shopIndex).getId();
        User currentUser = Session.getCurrentUser();

        try {
            // Create ServiceRequest with proper Supabase schema
            ServiceRequest request = new ServiceRequest();
            request.setClientId(currentUser.getId());
            request.setVehicleId(vehicleId);
            request.setShopId(shopId);
            request.setStatus("Pending");
            request.setTotalPriceEstimated(budget); // Use budget as estimated price if available

            // Store service description in helper field (will need ServiceRequestItem
            // later)
            request.setServiceDescription(String.join(", ", services));

            // Save to Supabase
            ServiceRequest createdRequest = serviceRequestService.create(request);

            System.out.println("=== New service request created ===");
            System.out.println("Request ID: " + createdRequest.getId());
            System.out.println("Vehicle: " + vehicleSelection);
            System.out.println("Shop: " + shopSelection);
            System.out.println("Services: " + String.join(", ", services));

            messageLabel.setStyle("-fx-text-fill: #28a745;");
            messageLabel.setText("Service request submitted successfully!");

            // Clear form
            clearForm();

        } catch (Exception e) {
            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setText("Error submitting request: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void clearForm() {
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

    @FXML
    private void handleViewPayments() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/programminggroupproject/payment-view.fxml"));
            Scene scene = new Scene(loader.load(), 800, 600);
            Stage stage = (Stage) vehicleComboBox.getScene().getWindow();
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}