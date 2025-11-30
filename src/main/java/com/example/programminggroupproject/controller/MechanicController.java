package com.example.programminggroupproject.controller;

import com.example.programminggroupproject.model.ServiceRequest;
import com.example.programminggroupproject.model.User;
import com.example.programminggroupproject.service.DataService;
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

import java.io.IOException;

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

    @FXML
    public void initialize() {
        // Setup columns
        colClient.setCellValueFactory(new PropertyValueFactory<>("clientName"));
        colVehicle.setCellValueFactory(new PropertyValueFactory<>("vehicleInfo"));
        colService.setCellValueFactory(new PropertyValueFactory<>("serviceDescription"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("createdAt"));

        // Load data
        masterData.addAll(DataService.getInstance().getAllServiceRequests());

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
                return false;
            });
        });

        requestsTable.setItems(filteredData);
    }

    @FXML
    private void handleAccept() {
        ServiceRequest selected = requestsTable.getSelectionModel().getSelectedItem();
        if (selected != null && "Pending".equals(selected.getStatus())) {
            selected.setStatus("In Progress");
            requestsTable.refresh();
        }
    }

    @FXML
    private void handleReject() {
        ServiceRequest selected = requestsTable.getSelectionModel().getSelectedItem();
        if (selected != null && "Pending".equals(selected.getStatus())) {
            selected.setStatus("Rejected");
            requestsTable.refresh();
        }
    }

    @FXML
    private void handleComplete() {
        ServiceRequest selected = requestsTable.getSelectionModel().getSelectedItem();
        if (selected != null && "In Progress".equals(selected.getStatus())) {
            selected.setStatus("Completed");
            requestsTable.refresh();
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
