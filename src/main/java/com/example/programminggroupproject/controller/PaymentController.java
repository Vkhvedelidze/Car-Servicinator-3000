package com.example.programminggroupproject.controller;

import com.example.programminggroupproject.CarServiceApp;
import com.example.programminggroupproject.model.Payment;
import com.example.programminggroupproject.model.ServiceRequest;
import com.example.programminggroupproject.service.PaymentService;
import com.example.programminggroupproject.service.ServiceRequestService;
import com.example.programminggroupproject.session.Session;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

public class PaymentController {

    @FXML private TableView<Payment> paymentsTable;
    @FXML private TableColumn<Payment, String> colDescription;
    @FXML private TableColumn<Payment, String> colAmount;
    @FXML private TableColumn<Payment, String> colStatus;
    @FXML private TableColumn<Payment, String> colDate;
    @FXML private Label messageLabel;

    private final PaymentService paymentService = PaymentService.getInstance();
    private final ServiceRequestService serviceRequestService = ServiceRequestService.getInstance();
    private final ObservableList<Payment> paymentList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Service description column
        colDescription.setCellValueFactory(cellData -> {
            Payment payment = cellData.getValue();
            return serviceRequestService.get(payment.getServiceRequestId())
                    .map(sr -> new SimpleStringProperty(sr.getDescription() != null ? sr.getDescription() : "N/A"))
                    .orElse(new SimpleStringProperty("N/A"));
        });

        // Amount column
        colAmount.setCellValueFactory(cellData -> {
            BigDecimal amount = cellData.getValue().getAmount();
            return new SimpleStringProperty(amount != null ? "€" + amount.toString() : "N/A");
        });

        // Status column
        colStatus.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getStatus()));

        // Date column
        colDate.setCellValueFactory(cellData -> {
            var createdAt = cellData.getValue().getCreatedAt();
            if (createdAt != null) {
                return new SimpleStringProperty(
                    createdAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
            }
            return new SimpleStringProperty("N/A");
        });

        loadPayments();
    }

    private void loadPayments() {
        UUID clientId = Session.getCurrentUser().getId();
        List<ServiceRequest> clientRequests = serviceRequestService.getByClientId(clientId);

        paymentList.clear();
        for (ServiceRequest request : clientRequests) {
            paymentService.getPaymentForServiceRequest(request.getId())
                    .ifPresent(paymentList::add);
        }
        paymentsTable.setItems(paymentList);
    }

    @FXML
    private void handlePay() {
        Payment selected = paymentsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setText("Please select a payment.");
            return;
        }

        if (!"Pending".equals(selected.getStatus())) {
            messageLabel.setStyle("-fx-text-fill: orange;");
            messageLabel.setText("This payment is already " + selected.getStatus());
            return;
        }

        try {
            paymentService.markAsCompleted(selected.getId());
            messageLabel.setStyle("-fx-text-fill: green;");
            messageLabel.setText("Payment successful!");
            loadPayments();
        } catch (Exception e) {
            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setText("Payment failed: " + e.getMessage());
        }
    }

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(CarServiceApp.class.getResource("client-view.fxml"));
            Scene scene = new Scene(loader.load(), 800, 600);
            Stage stage = (Stage) paymentsTable.getScene().getWindow();
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}