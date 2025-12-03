package com.example.programminggroupproject.controller;

import com.example.programminggroupproject.model.ServiceRequest;
import com.example.programminggroupproject.model.Vehicle;
import com.example.programminggroupproject.service.ServiceRequestService;
import com.example.programminggroupproject.service.VehicleService;
import com.example.programminggroupproject.session.Session;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class ClientRequestsController {

    @FXML
    private TableView<ServiceRequest> requestsTable;

    @FXML
    private TableColumn<ServiceRequest, String> idColumn;

    @FXML
    private TableColumn<ServiceRequest, String> vehicleColumn;

    @FXML
    private TableColumn<ServiceRequest, String> servicesColumn;

    @FXML
    private TableColumn<ServiceRequest, String> statusColumn;

    @FXML
    private TableColumn<ServiceRequest, OffsetDateTime> dateColumn;

    @FXML
    private Label messageLabel;

    // Filter controls
    @FXML
    private ComboBox<String> statusFilterComboBox;

    @FXML
    private ComboBox<String> vehicleFilterComboBox;

    @FXML
    private TextField searchField;

    private final ServiceRequestService serviceRequestService = ServiceRequestService.getInstance();
    private final VehicleService vehicleService = VehicleService.getInstance();
    
    // Store all requests for filtering
    private List<ServiceRequest> allRequests;
    private List<Vehicle> userVehicles;

    @FXML
    public void initialize() {
        // Set up table columns
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        vehicleColumn.setCellValueFactory(new PropertyValueFactory<>("vehicleInfo"));
        servicesColumn.setCellValueFactory(new PropertyValueFactory<>("serviceDescription"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("createdAt"));

        // Initialize filter controls
        initializeFilters();

        // Load requests for current user
        loadClientRequests();
    }

    /**
     * Initialize filter controls with options
     */
    private void initializeFilters() {
        // Status filter options
        statusFilterComboBox.getItems().addAll(
            "All Statuses",
            "Pending",
            "In Progress",
            "Completed",
            "Cancelled"
        );
        statusFilterComboBox.setValue("All Statuses");

        // Vehicle filter will be populated after loading vehicles
        vehicleFilterComboBox.getItems().add("All Vehicles");
        vehicleFilterComboBox.setValue("All Vehicles");
    }


    /**
     * Load all service requests for the current user
     */
    private void loadClientRequests() {
        if (Session.getCurrentUser() == null) {
            messageLabel.setText("No user logged in");
            return;
        }
    
        try {
            UUID clientId = Session.getCurrentUser().getId();
            
            // Load user's vehicles for filter dropdown
            userVehicles = vehicleService.getByClientId(clientId);
            updateVehicleFilter();
            
            // Load all service requests for the client
            allRequests = serviceRequestService.getByClientId(clientId);
    
            // Enrich with vehicle info
            for (ServiceRequest request : allRequests) {
                enrichRequestWithVehicleInfo(request);
            }
    
            // Display all requests initially
            displayRequests(allRequests);
            
            // Update message
            messageLabel.setStyle("-fx-text-fill: #666;");
            messageLabel.setText(allRequests.size() + " service request(s) found");
            
        } catch (Exception e) {
            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setText("Error loading requests: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Enrich a service request with vehicle information
     */
    private void enrichRequestWithVehicleInfo(ServiceRequest request) {
        if (request.getVehicleId() != null) {
            Vehicle vehicle = vehicleService.get(request.getVehicleId()).orElse(null);
            if (vehicle != null) {
                request.setVehicleInfo(vehicle.getFullInfo());
            } else {
                request.setVehicleInfo("Unknown Vehicle");
            }
        }
    }

    /**
     * Update vehicle filter dropdown with user's vehicles
     */
    private void updateVehicleFilter() {
        vehicleFilterComboBox.getItems().clear();
        vehicleFilterComboBox.getItems().add("All Vehicles");
        
        for (Vehicle vehicle : userVehicles) {
            vehicleFilterComboBox.getItems().add(vehicle.getFullInfo());
        }
        
        vehicleFilterComboBox.setValue("All Vehicles");
    }

    /**
     * Display a filtered list of service requests in the table
     */
    private void displayRequests(List<ServiceRequest> requests) {
        ObservableList<ServiceRequest> requestList = FXCollections.observableArrayList(requests);
        requestsTable.setItems(requestList);
    }

    /**
     * Apply filters to the service requests
     */
    @FXML
    private void handleApplyFilter() {
        if (allRequests == null || allRequests.isEmpty()) {
            messageLabel.setStyle("-fx-text-fill: #666;");
            messageLabel.setText("No requests to filter");
            return;
        }

        try {
            String statusFilter = statusFilterComboBox.getValue();
            String vehicleFilter = vehicleFilterComboBox.getValue();
            String searchText = searchField.getText().trim().toLowerCase();

            // Start with all requests
            List<ServiceRequest> filtered = allRequests.stream()
                .filter(request -> {
                    // Filter by status
                    if (statusFilter != null && !statusFilter.equals("All Statuses")) {
                        if (!statusFilter.equals(request.getStatus())) {
                            return false;
                        }
                    }

                    // Filter by vehicle
                    if (vehicleFilter != null && !vehicleFilter.equals("All Vehicles")) {
                        if (request.getVehicleInfo() == null || 
                            !request.getVehicleInfo().equals(vehicleFilter)) {
                            return false;
                        }
                    }

                    // Filter by search text (searches in service description)
                    if (!searchText.isEmpty()) {
                        String services = request.getServiceDescription();
                        if (services == null || 
                            !services.toLowerCase().contains(searchText)) {
                            return false;
                        }
                    }

                    return true;
                })
                .collect(Collectors.toList());

            displayRequests(filtered);
            
            // Update message
            messageLabel.setStyle("-fx-text-fill: #666;");
            messageLabel.setText(filtered.size() + " of " + allRequests.size() + " request(s) match filter");
            
        } catch (Exception e) {
            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setText("Error applying filter: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Clear all filters and show all requests
     */
    @FXML
    private void handleClearFilter() {
        // Reset filter controls
        statusFilterComboBox.setValue("All Statuses");
        vehicleFilterComboBox.setValue("All Vehicles");
        searchField.clear();

        // Display all requests
        if (allRequests != null) {
            displayRequests(allRequests);
            messageLabel.setStyle("-fx-text-fill: #666;");
            messageLabel.setText(allRequests.size() + " service request(s) found");
        }
    }

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/programminggroupproject/dashboard.fxml"));
            Scene scene = new Scene(loader.load(), 800, 600);

            // Load CSS if available
            try {
                String css = getClass().getResource("/com/example/programminggroupproject/styles.css").toExternalForm();
                scene.getStylesheets().add(css);
            } catch (Exception e) {
                // Ignore
            }

            DashboardController controller = loader.getController();
            controller.setUser(Session.getCurrentUser());

            Stage stage = (Stage) requestsTable.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Dashboard - Car Servicinator 3000");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
