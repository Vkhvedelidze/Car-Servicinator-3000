package com.example.programminggroupproject.controller;

import com.example.programminggroupproject.model.ServiceRequest;
import com.example.programminggroupproject.service.ServiceRequestService;
import com.example.programminggroupproject.session.Session;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AdminController {

    @FXML
    private PieChart statusPieChart;

    @FXML
    private BarChart<String, Number> revenueBarChart;

    private final ServiceRequestService serviceRequestService = ServiceRequestService.getInstance();

    @FXML
    public void initialize() {
        loadAnalytics();
    }

    private void loadAnalytics() {
        try {
            // Get all service requests from Supabase
            List<ServiceRequest> requests = serviceRequestService.getAll();

            // 1. Status Pie Chart
            Map<String, Long> statusCounts = requests.stream()
                    .collect(Collectors.groupingBy(
                            r -> r.getStatus() == null ? "Unknown" : r.getStatus(),
                            Collectors.counting()));

            ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
            statusCounts.forEach((status, count) -> pieData.add(new PieChart.Data(status, count)));
            statusPieChart.setData(pieData);

            // 2. Service Description Bar Chart
            Map<String, Long> serviceCounts = requests.stream()
                    .filter(r -> r.getServiceDescription() != null && !r.getServiceDescription().isEmpty())
                    .collect(Collectors.groupingBy(
                            ServiceRequest::getServiceDescription,
                            Collectors.counting()));

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Service Counts");

            // Limit to top 10 services for readability
            serviceCounts.entrySet().stream()
                    .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                    .limit(10)
                    .forEach((entry) -> {
                        String serviceName = entry.getKey();
                        // Truncate long service names
                        if (serviceName.length() > 30) {
                            serviceName = serviceName.substring(0, 27) + "...";
                        }
                        series.getData().add(new XYChart.Data<>(serviceName, entry.getValue()));
                    });

            revenueBarChart.getData().add(series);

        } catch (Exception e) {
            System.err.println("Error loading analytics: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/programminggroupproject/dashboard.fxml"));
            Scene scene = new Scene(loader.load(), 800, 600);

            try {
                String css = getClass().getResource("/com/example/programminggroupproject/styles.css").toExternalForm();
                scene.getStylesheets().add(css);
            } catch (Exception e) {
            }

            DashboardController controller = loader.getController();
            controller.setUser(Session.getCurrentUser());

            Stage stage = (Stage) statusPieChart.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Dashboard - Car Servicinator 3000");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
