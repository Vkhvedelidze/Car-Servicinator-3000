package com.example.programminggroupproject.controller;

import com.example.programminggroupproject.model.ServiceRequest;
import com.example.programminggroupproject.model.Vehicle;
import com.example.programminggroupproject.service.ServiceRequestService;
import com.example.programminggroupproject.service.UserService;
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

import java.util.List;
import java.io.IOException;
import java.util.stream.Collectors;
import com.example.programminggroupproject.service.PaymentService;
import com.example.programminggroupproject.model.Payment;
import java.math.BigDecimal;
import java.net.URL;
import javafx.scene.control.Alert;
import java.util.List;
import javafx.scene.control.TextInputDialog;
import java.util.Optional;

public class MechanicController {

    @FXML
    private TextField searchField;

    @FXML
    private TableView<ServiceRequest> requestsTable;

    @FXML
    private TableColumn<ServiceRequest, String> colClient;

    @FXML
    private TableColumn<ServiceRequest, String> colVehicle;

    @FXML
    private TableColumn<ServiceRequest, String> colService;

    @FXML
    private TableColumn<ServiceRequest, String> colStatus;

    @FXML
    private TableColumn<ServiceRequest, String> colDate;

    // Filter controls
    @FXML
    private ComboBox<String> statusFilterComboBox;

    @FXML
    private ComboBox<String> clientFilterComboBox;

    @FXML
    private Label filterResultsLabel;
    @FXML
    private TableColumn<ServiceRequest, String> colNotes;

    private ObservableList<ServiceRequest> masterData = FXCollections.observableArrayList();
    private List<ServiceRequest> allRequests; // Store all requests for filtering
    
    private final ServiceRequestService serviceRequestService = ServiceRequestService.getInstance();
    private final PaymentService paymentService = PaymentService.getInstance();
    private final VehicleService vehicleService = VehicleService.getInstance();
    private final UserService userService = UserService.getInstance();

    @FXML
    public void initialize() {
        // Setup columns
        colClient.setCellValueFactory(new PropertyValueFactory<>("clientName"));
        colVehicle.setCellValueFactory(new PropertyValueFactory<>("vehicleInfo"));
        colService.setCellValueFactory(new PropertyValueFactory<>("serviceDescription"));
        colNotes.setCellValueFactory(new PropertyValueFactory<>("notes"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("createdAt"));

        // Initialize filter controls
        initializeFilters();

        // Load data from Supabase
        loadServiceRequests();
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
            "Rejected"
        );
        statusFilterComboBox.setValue("All Statuses");

        // Client filter will be populated after loading requests
        clientFilterComboBox.getItems().add("All Clients");
        clientFilterComboBox.setValue("All Clients");
    }

    /**
     * Load all service requests for the mechanic's shop
     */
    private void loadServiceRequests() {
        try {
            if (Session.getCurrentUser() == null || Session.getCurrentUser().getShopId() == null) {
                System.err.println("No user or shop ID found");
                return;
            }

            // Get all service requests from Supabase for this shop
            List<ServiceRequest> requests = serviceRequestService.getByShopId(
                Session.getCurrentUser().getShopId()
            );

            // Populate display fields and collect unique clients
            List<String> uniqueClients = new java.util.ArrayList<>();
            
            for (ServiceRequest request : requests) {
                // Try to fetch client name
                if (request.getClientId() != null) {
                    try {
                        var userOptional = userService.get(request.getClientId());
                        if (userOptional.isPresent()) {
                            String clientName = userOptional.get().getFullName();
                            request.setClientName(clientName);
                            if (!uniqueClients.contains(clientName)) {
                                uniqueClients.add(clientName);
                            }
                        } else {
                            request.setClientName("Unknown Client");
                        }
                    } catch (Exception e) {
                        System.err.println("Error fetching client (RLS issue?): " + e.getMessage());
                        // Fallback to ID if name fetch fails
                        request.setClientName("Client " + request.getClientId().toString().substring(0, 8));
                    }
                } else {
                    request.setClientName("No Client");
                }

                // Fetch and set vehicle info
                if (request.getVehicleId() != null) {
                    try {
                        var vehicleOptional = vehicleService.get(request.getVehicleId());

                        if (vehicleOptional.isPresent()) {
                            Vehicle vehicle = vehicleOptional.get();
                            String vehicleInfo = vehicle.getMake() + " " + vehicle.getModel() +
                                    " - " + vehicle.getLicensePlate();
                            request.setVehicleInfo(vehicleInfo);
                        } else {
                            request.setVehicleInfo("Vehicle Not Found");
                        }
                    } catch (Exception e) {
                        System.err.println("Error fetching vehicle: " + e.getMessage());
                        request.setVehicleInfo("Error loading vehicle");
                    }
                } else {
                    request.setVehicleInfo("No Vehicle");
                }
            }

            // Store all requests for filtering
            allRequests = requests;

            // Update client filter dropdown
            updateClientFilter(uniqueClients);

            // Display all requests
            displayRequests(requests);
            
            // Update results count
            updateFilterResultsLabel(requests.size(), requests.size());

        } catch (Exception e) {
            System.err.println("Error loading service requests: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Update client filter dropdown with unique clients
     */
    private void updateClientFilter(List<String> clients) {
        clientFilterComboBox.getItems().clear();
        clientFilterComboBox.getItems().add("All Clients");
        clientFilterComboBox.getItems().addAll(clients.stream().sorted().collect(Collectors.toList()));
        clientFilterComboBox.setValue("All Clients");
    }

    /**
     * Display service requests in the table
     */
    private void displayRequests(List<ServiceRequest> requests) {
        masterData.clear();
        masterData.addAll(requests);
        requestsTable.setItems(masterData);
    }

    /**
     * Update the filter results label
     */
    private void updateFilterResultsLabel(int filtered, int total) {
        if (filtered == total) {
            filterResultsLabel.setText(total + " service request(s) found");
        } else {
            filterResultsLabel.setText(filtered + " of " + total + " request(s) match filter");
        }
    }

    /**
     * Apply filters to the service requests
     */
    @FXML
    private void handleApplyFilter() {
        if (allRequests == null || allRequests.isEmpty()) {
            filterResultsLabel.setText("No requests to filter");
            return;
        }

        try {
            String statusFilter = statusFilterComboBox.getValue();
            String clientFilter = clientFilterComboBox.getValue();
            String searchText = searchField.getText().trim().toLowerCase();

            // Apply filters using Stream API
            List<ServiceRequest> filtered = allRequests.stream()
                .filter(request -> {
                    // Filter by status
                    if (statusFilter != null && !statusFilter.equals("All Statuses")) {
                        if (!statusFilter.equals(request.getStatus())) {
                            return false;
                        }
                    }

                    // Filter by client
                    if (clientFilter != null && !clientFilter.equals("All Clients")) {
                        if (request.getClientName() == null || 
                            !request.getClientName().equals(clientFilter)) {
                            return false;
                        }
                    }

                    // Filter by search text (searches vehicle and services)
                    if (!searchText.isEmpty()) {
                        boolean matchesVehicle = request.getVehicleInfo() != null && 
                            request.getVehicleInfo().toLowerCase().contains(searchText);
                        boolean matchesService = request.getServiceDescription() != null && 
                            request.getServiceDescription().toLowerCase().contains(searchText);
                        boolean matchesClient = request.getClientName() != null &&
                            request.getClientName().toLowerCase().contains(searchText);
                        
                        if (!matchesVehicle && !matchesService && !matchesClient) {
                            return false;
                        }
                    }

                    return true;
                })
                .collect(Collectors.toList());

            displayRequests(filtered);
            updateFilterResultsLabel(filtered.size(), allRequests.size());
            
        } catch (Exception e) {
            filterResultsLabel.setText("Error applying filter: " + e.getMessage());
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
        clientFilterComboBox.setValue("All Clients");
        searchField.clear();

        // Display all requests
        if (allRequests != null) {
            displayRequests(allRequests);
            updateFilterResultsLabel(allRequests.size(), allRequests.size());
        }
    }

    @FXML
    private void handleAccept() {
        ServiceRequest selected = requestsTable.getSelectionModel
        ().getSelectedItem();
        
        if (selected != null && "Pending".equals(selected.getStatus())) {
            try {
                serviceRequestService.assignMechanic(
                        selected.getId(),
                        Session.getCurrentUser().getId());
                loadServiceRequests();
                requestsTable.refresh();
            } catch (Exception e) {
                System.err.println("Error accepting request: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleReject() {
        ServiceRequest selected = requestsTable.getSelectionModel().getSelectedItem();
        if (selected != null && "Pending".equals(selected.getStatus())) {
            try {
                serviceRequestService.updateStatus(selected.getId(), "Rejected");
                loadServiceRequests();
                requestsTable.refresh();
            } catch (Exception e) {
                System.err.println("Error rejecting request: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleComplete() {
        ServiceRequest selected = requestsTable.getSelectionModel().getSelectedItem();
        System.out.println("Selected request: " + selected);
        System.out.println("Status: " + (selected != null ? selected.getStatus() : "null"));

        if (selected != null && "In Progress".equals(selected.getStatus())) {
            // Show dialog to enter final price
            TextInputDialog dialog = new TextInputDialog(
                selected.getTotalPriceEstimated() != null 
                    ? selected.getTotalPriceEstimated().toString() 
                    : "0.00"
            );
            dialog.setTitle("Complete Service");
            dialog.setHeaderText("Enter Final Price");
            dialog.setContentText("Final price (€):");

            Optional<String> result = dialog.showAndWait();
            if (result.isPresent()) {
                try {
                    BigDecimal finalPrice = new BigDecimal(result.get().replace(",", "."));
                    
                    if (finalPrice.compareTo(BigDecimal.ZERO) < 0) {
                        showError("Price cannot be negative.");
                        return;
                    }

                    // Update the service request with final price
                    selected.setTotalPriceFinal(finalPrice);
                    serviceRequestService.updateStatus(selected.getId(), "Completed");
                    
                    // Also update the final price in database
                    ServiceRequest updated = serviceRequestService.get(selected.getId()).orElse(selected);
                    updated.setTotalPriceFinal(finalPrice);
                    serviceRequestService.update(selected.getId(), updated);

                    // Create payment with the final price
                    Payment payment = new Payment();
                    payment.setServiceRequestId(selected.getId());
                    payment.setAmount(finalPrice);  // Use final price entered by mechanic
                    payment.setStatus("Pending");
                    paymentService.create(payment);

                    loadServiceRequests();
                    requestsTable.refresh();
                } catch (NumberFormatException e) {
                    showError("Invalid price format. Please enter a valid number.");
                } catch (Exception e) {
                    showError("Error completing request: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        } else {
            showError("Please select a request that is 'In Progress'.");
        }
    }

    @FXML
    private void handleBack() {
        try {
            URL fxmlUrl = getClass().getResource("/com/example/programminggroupproject/dashboard.fxml");
            if (fxmlUrl == null) {
                showError("Could not find dashboard.fxml");
                return;
            }
            
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Scene scene = new Scene(loader.load(), 800, 600);

            // Load CSS if available
            URL cssUrl = getClass().getResource("/com/example/programminggroupproject/styles.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            }

            // Set the current user in dashboard controller
            DashboardController controller = loader.getController();
            controller.setUser(Session.getCurrentUser());

            Stage stage = (Stage) requestsTable.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Dashboard - Car Servicinator 3000");
        } catch (IOException e) {
            // Use a logging framework in production
            e.printStackTrace();
            showError("Failed to load dashboard: " + e.getMessage());
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setContentText(message);
        alert.showAndWait();
    }
}