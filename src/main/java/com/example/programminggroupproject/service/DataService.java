package com.example.programminggroupproject.service;

import com.example.programminggroupproject.model.User;
import com.example.programminggroupproject.model.ServiceRequest; // Assuming this exists or I need to create/update it
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class DataService {

    private static DataService instance;

    private List<User> users;
    private List<ServiceRequest> serviceRequests;

    private DataService() {
        users = new ArrayList<>();
        serviceRequests = new ArrayList<>();
        initializeMockData();
    }

    public static synchronized DataService getInstance() {
        if (instance == null) {
            instance = new DataService();
        }
        return instance;
    }

    private void initializeMockData() {
        // Mock Users
        users.add(new User("client", "client123", "client", "Client User", "client@example.com"));
        users.add(new User("mechanic", "mechanic123", "mechanic", "Mechanic User", "mechanic@example.com"));
        users.add(new User("admin", "admin123", "admin", "Admin User", "admin@example.com"));

        // Mock Service Requests
        ServiceRequest req1 = new ServiceRequest();
        req1.setId(1L);
        req1.setClientName("Client User");
        req1.setVehicleInfo("Toyota Corolla - ABC123");
        req1.setServiceDescription("Oil & Filters, General check-up");
        req1.setStatus("Pending");
        req1.setCreatedAt(java.time.LocalDateTime.now().minusDays(1));
        serviceRequests.add(req1);

        ServiceRequest req2 = new ServiceRequest();
        req2.setId(2L);
        req2.setClientName("Client User");
        req2.setVehicleInfo("Honda Civic - XYZ789");
        req2.setServiceDescription("Brakes");
        req2.setStatus("In Progress");
        req2.setCreatedAt(java.time.LocalDateTime.now().minusHours(5));
        serviceRequests.add(req2);
    }

    // User Methods
    public Optional<User> authenticate(String username, String password) {
        return users.stream()
                .filter(u -> u.getUsername().equals(username) && u.getPassword().equals(password))
                .findFirst();
    }

    public boolean registerUser(User user) {
        if (users.stream().anyMatch(u -> u.getUsername().equals(user.getUsername()))) {
            return false; // Username exists
        }
        users.add(user);
        return true;
    }

    public List<User> getAllUsers() {
        return new ArrayList<>(users);
    }

    // Service Request Methods
    public void addServiceRequest(ServiceRequest request) {
        serviceRequests.add(request);
    }

    public List<ServiceRequest> getAllServiceRequests() {
        return new ArrayList<>(serviceRequests);
    }

    public List<ServiceRequest> getServiceRequestsByMechanic(String mechanicUsername) {
        // For now, return all or filter if we add mechanic assignment field
        return serviceRequests;
    }

    public List<ServiceRequest> getServiceRequestsByClient(String clientName) {
        return serviceRequests.stream()
                .filter(r -> r.getClientName() != null && r.getClientName().equals(clientName))
                .collect(Collectors.toList());
    }
}
