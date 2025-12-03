package com.example.programminggroupproject.controller;

import com.example.programminggroupproject.model.ServiceRequest;
import com.example.programminggroupproject.service.ServiceRequestService;
import com.example.programminggroupproject.session.Session;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;

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

    private final ServiceRequestService serviceRequestService = ServiceRequestService.getInstance();

    @FXML
    public void initialize() {
        // Set up table columns
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        vehicleColumn.setCellValueFactory(new PropertyValueFactory<>("vehicleInfo"));
        servicesColumn.setCellValueFactory(new PropertyValueFactory<>("serviceDescription"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("createdAt"));

        // Load requests for current user
        loadClientRequests();
    }

    private void loadClientRequests() {
        if (Session.getCurrentUser() == null) {
            messageLabel.setText("No user logged in");
            return;
        }

        try {
            // Get requests by client ID from Supabase
            List<ServiceRequest> requests = serviceRequestService.getByClientId(
                    Session.getCurrentUser().getId());

            ObservableList<ServiceRequest> requestList = FXCollections.observableArrayList(requests);
            requestsTable.setItems(requestList);

            if (requests.isEmpty()) {
                messageLabel.setText("You have no service requests yet.");
            } else {
                messageLabel.setText("Showing " + requests.size() + " request(s)");
            }
        } catch (Exception e) {
            messageLabel.setText("Error loading requests: " + e.getMessage());
            e.printStackTrace();
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
