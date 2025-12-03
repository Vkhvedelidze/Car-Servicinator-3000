package com.example.programminggroupproject.controller;

import com.example.programminggroupproject.model.Payment;
import com.example.programminggroupproject.model.ServiceRequest;
import com.example.programminggroupproject.service.PaymentService;
import com.example.programminggroupproject.service.ServiceRequestService;
import com.example.programminggroupproject.session.Session;
import com.example.programminggroupproject.service.UserService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AdminController {

    @FXML
    private Label totalRevenueLabel;
    @FXML
    private Label totalRequestsLabel;
    @FXML
    private Label activeRequestsLabel;
    @FXML
    private Label avgTicketLabel;

    @FXML
    private PieChart statusPieChart;

    @FXML
    private BarChart<String, Number> revenueBarChart;

    @FXML
    private LineChart<String, Number> revenueLineChart;

    @FXML
    private BarChart<String, Number> mechanicRevenueChart;

    private final ServiceRequestService serviceRequestService = ServiceRequestService.getInstance();
    private final PaymentService paymentService = PaymentService.getInstance();
    private final UserService userService = UserService.getInstance();

    @FXML
    public void initialize() {
        loadAnalytics();
    }

    private void loadAnalytics() {
        try {
            // Get all service requests and payments
            List<ServiceRequest> requests = serviceRequestService.getAll();
            List<Payment> completedPayments = paymentService.getCompletedPayments();

            // Get users safely
            List<com.example.programminggroupproject.model.User> allUsers;
            try {
                System.out.println("DEBUG: Attempting to fetch all users via UserService...");
                if (userService == null) {
                    System.err.println("DEBUG: userService is null!");
                }
                allUsers = userService.getAll();
                System.out.println("DEBUG: Successfully fetched " + allUsers.size() + " users.");
            } catch (Exception e) {
                System.err.println("DEBUG: Error fetching users in AdminController:");
                System.err.println("DEBUG: Message: " + e.getMessage());
                System.err.println("DEBUG: Cause: " + e.getCause());
                e.printStackTrace();
                allUsers = java.util.Collections.emptyList();
            }

            Map<java.util.UUID, String> mechanicNames = allUsers.stream()
                    .filter(u -> "mechanic".equalsIgnoreCase(u.getRole()))
                    .collect(Collectors.toMap(
                            com.example.programminggroupproject.model.User::getId,
                            u -> u.getFullName() != null ? u.getFullName() : "Unknown Mechanic",
                            (existing, replacement) -> existing));

            // 1. KPIs
            // Total Revenue
            java.math.BigDecimal totalRevenue = paymentService.calculateTotal(completedPayments);
            totalRevenueLabel.setText(String.format("€%.2f", totalRevenue));

            // Total Requests
            totalRequestsLabel.setText(String.valueOf(requests.size()));

            // Active Requests
            long activeRequests = requests.stream()
                    .filter(r -> !"Completed".equalsIgnoreCase(r.getStatus())
                            && !"Cancelled".equalsIgnoreCase(r.getStatus()))
                    .count();
            activeRequestsLabel.setText(String.valueOf(activeRequests));

            // Avg Ticket Size
            if (!completedPayments.isEmpty()) {
                java.math.BigDecimal avgTicket = totalRevenue.divide(
                        java.math.BigDecimal.valueOf(completedPayments.size()),
                        2,
                        java.math.RoundingMode.HALF_UP);
                avgTicketLabel.setText(String.format("€%.2f", avgTicket));
            } else {
                avgTicketLabel.setText("€0.00");
            }

            // 2. Status Pie Chart
            Map<String, Long> statusCounts = requests.stream()
                    .collect(Collectors.groupingBy(
                            r -> r.getStatus() == null ? "Unknown" : r.getStatus(),
                            Collectors.counting()));

            ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
            statusCounts.forEach((status, count) -> pieData.add(new PieChart.Data(status, count)));
            statusPieChart.setData(pieData);

            // 3. Revenue by Service Type Bar Chart
            Map<String, java.math.BigDecimal> revenueByService = new java.util.HashMap<>();
            Map<java.util.UUID, java.math.BigDecimal> revenueByMechanic = new java.util.HashMap<>();

            // Optimization: Create a map of requests by ID to avoid N+1 DB calls
            Map<java.util.UUID, ServiceRequest> requestMap = requests.stream()
                    .collect(Collectors.toMap(ServiceRequest::getId, r -> r));

            for (Payment payment : completedPayments) {
                if (payment.getServiceRequestId() != null) {
                    // Use in-memory map instead of making a DB call for every payment
                    ServiceRequest request = requestMap.get(payment.getServiceRequestId());

                    if (request != null) {
                        // Service Type Revenue
                        String service = request.getServiceDescription();
                        if (service == null || service.isEmpty())
                            service = "Unknown Service";
                        revenueByService.merge(service, payment.getAmount(), java.math.BigDecimal::add);

                        // Mechanic Revenue
                        if (request.getMechanicId() != null) {
                            revenueByMechanic.merge(request.getMechanicId(), payment.getAmount(),
                                    java.math.BigDecimal::add);
                        }
                    }
                }
            }

            XYChart.Series<String, Number> barSeries = new XYChart.Series<>();
            barSeries.setName("Revenue");

            revenueByService.entrySet().stream()
                    .sorted(Map.Entry.<String, java.math.BigDecimal>comparingByValue().reversed())
                    .limit(10)
                    .forEach(entry -> {
                        String serviceName = entry.getKey();
                        if (serviceName.length() > 20)
                            serviceName = serviceName.substring(0, 17) + "...";
                        barSeries.getData().add(new XYChart.Data<>(serviceName, entry.getValue()));
                    });

            revenueBarChart.getData().clear();
            revenueBarChart.getData().add(barSeries);

            // 4. Revenue Trends Line Chart (Last 7 Days)
            java.time.LocalDate today = java.time.LocalDate.now();
            java.time.LocalDate sevenDaysAgo = today.minusDays(6);

            Map<java.time.LocalDate, java.math.BigDecimal> revenueByDate = new java.util.TreeMap<>();

            // Initialize last 7 days with 0
            for (int i = 0; i < 7; i++) {
                revenueByDate.put(sevenDaysAgo.plusDays(i), java.math.BigDecimal.ZERO);
            }

            for (Payment payment : completedPayments) {
                if (payment.getCreatedAt() != null) {
                    java.time.LocalDate date = payment.getCreatedAt().toLocalDate();
                    if (!date.isBefore(sevenDaysAgo) && !date.isAfter(today)) {
                        revenueByDate.merge(date, payment.getAmount(), java.math.BigDecimal::add);
                    }
                }
            }

            XYChart.Series<String, Number> lineSeries = new XYChart.Series<>();
            lineSeries.setName("Daily Revenue");

            java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("MM-dd");
            revenueByDate.forEach(
                    (date, amount) -> lineSeries.getData().add(new XYChart.Data<>(date.format(formatter), amount)));

            revenueLineChart.getData().clear();
            revenueLineChart.getData().add(lineSeries);

            // 5. Top Mechanics by Revenue
            XYChart.Series<String, Number> mechanicSeries = new XYChart.Series<>();
            mechanicSeries.setName("Revenue");

            // Use revenueByMechanic to ensure we show data even if user fetch failed
            // If we have mechanic names, we can also add mechanics with 0 revenue
            Map<String, java.math.BigDecimal> displayData = new java.util.HashMap<>();

            // First add all mechanics with revenue
            revenueByMechanic.forEach((id, revenue) -> {
                String name = mechanicNames.getOrDefault(id, "Mechanic " + id.toString().substring(0, 8));
                displayData.put(name, revenue);
            });

            // Then add mechanics with 0 revenue (only if we successfully fetched users)
            mechanicNames.forEach((id, name) -> {
                displayData.putIfAbsent(name, java.math.BigDecimal.ZERO);
            });

            displayData.entrySet().stream()
                    .sorted(Map.Entry.<String, java.math.BigDecimal>comparingByValue().reversed())
                    .limit(10) // Limit to top 10 to avoid overcrowding
                    .forEach(entry -> {
                        mechanicSeries.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
                    });

            mechanicRevenueChart.getData().clear();
            mechanicRevenueChart.getData().add(mechanicSeries);

        } catch (Exception e) {
            System.err.println("Error loading analytics: " + e.getMessage());
            e.printStackTrace();
            // Display error in UI
            if (totalRevenueLabel != null) {
                totalRevenueLabel.setText("Error");
                totalRevenueLabel.setStyle("-fx-text-fill: red; -fx-font-size: 14px;");
            }
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
