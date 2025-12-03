package com.example.programminggroupproject.controller;

import com.example.programminggroupproject.model.ServiceRequest;
import com.example.programminggroupproject.model.Vehicle;
import com.example.programminggroupproject.service.ServiceRequestService;
import com.example.programminggroupproject.service.UserService;
import com.example.programminggroupproject.service.VehicleService;
import com.example.programminggroupproject.session.Session;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.util.List;
import java.io.IOException;
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

    private ObservableList<ServiceRequest> masterData = FXCollections.observableArrayList();
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
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("createdAt"));

        // Load data from Supabase
        loadServiceRequests();

        // Wrap in FilteredList
        FilteredList<ServiceRequest> filteredData = new FilteredList<>(masterData, p -> true);

        // Bind search field
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(request -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                String lowerCaseFilter = newValue.toLowerCase();

                if (request.getClientName() != null
                        && request.getClientName().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }
                if (request.getVehicleInfo() != null
                        && request.getVehicleInfo().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }
                if (request.getServiceDescription() != null
                        && request.getServiceDescription().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }
                return false;
            });
        });

        requestsTable.setItems(filteredData);
    }

    private void loadServiceRequests() {
        try {
            // Get all service requests from Supabase
            List<ServiceRequest> requests = serviceRequestService.getByShopId(Session.getCurrentUser().getShopId());

            // Populate display fields
            for (ServiceRequest request : requests) {
                // Try to fetch client name
                if (request.getClientId() != null) {
                    try {
                        var userOptional = userService.get(request.getClientId());
                        if (userOptional.isPresent()) {
                            request.setClientName(userOptional.get().getFullName());
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

            masterData.clear();
            masterData.addAll(requests);

        } catch (Exception e) {
            System.err.println("Error loading service requests: " + e.getMessage());
            e.printStackTrace();
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