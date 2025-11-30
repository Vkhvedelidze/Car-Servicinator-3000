package com.example.programminggroupproject.controller;

import com.example.programminggroupproject.model.ServiceRequest;
import com.example.programminggroupproject.service.DataService;
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

    @FXML
    public void initialize() {
        loadAnalytics();
    }

    private void loadAnalytics() {
        List<ServiceRequest> requests = DataService.getInstance().getAllServiceRequests();

        // 1. Status Pie Chart
        Map<String, Long> statusCounts = requests.stream()
                .collect(Collectors.groupingBy(
                        r -> r.getStatus() == null ? "Unknown" : r.getStatus(),
                        Collectors.counting()));

        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
        statusCounts.forEach((status, count) -> pieData.add(new PieChart.Data(status, count)));
        statusPieChart.setData(pieData);

        // 2. Revenue Bar Chart (Mock revenue based on service type count for now, or
        // random)
        // Since we don't have prices in the simple mock, let's just count service types
        Map<String, Long> serviceCounts = requests.stream()
                .collect(Collectors.groupingBy(
                        r -> r.getServiceDescription() == null ? "Other" : r.getServiceDescription(),
                        Collectors.counting()));

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Service Counts");
        serviceCounts.forEach((service, count) -> series.getData().add(new XYChart.Data<>(service, count)));

        revenueBarChart.getData().add(series);
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
