package com.example.programminggroupproject.service;

import com.example.programminggroupproject.model.Vehicle;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for managing Vehicle entities with Supabase backend.
 * Provides vehicle-specific operations in addition to standard CRUD.
 */
public class VehicleService extends BaseSupabaseService<Vehicle> {
    
    private static VehicleService instance;
    
    private VehicleService() {
        super("vehicles", Vehicle.class, new TypeReference<List<Vehicle>>() {});
    }
    
    /**
     * Get singleton instance of VehicleService
     */
    public static synchronized VehicleService getInstance() {
        if (instance == null) {
            instance = new VehicleService();
        }
        return instance;
    }
    
    // ==================== VEHICLE-SPECIFIC OPERATIONS ====================
    
    /**
     * Get all vehicles for a specific client
     * @param clientId The client's user ID (UUID)
     * @return List of vehicles owned by the client
     */
    public List<Vehicle> getByClientId(UUID clientId) {
        return findBy("client_id", clientId);
    }
    
    /**
     * Find vehicle by license plate
     * @param licensePlate The license plate number
     * @return Optional containing the vehicle if found
     */
    public Optional<Vehicle> findByLicensePlate(String licensePlate) {
        return findOneBy("license_plate", licensePlate);
    }
    
    /**
     * Check if a license plate already exists
     * @param licensePlate The license plate to check
     * @return true if license plate exists, false otherwise
     */
    public boolean licensePlateExists(String licensePlate) {
        return existsBy("license_plate", licensePlate);
    }
    
    /**
     * Get vehicles by make
     * @param make The vehicle make (e.g., "Toyota")
     * @return List of vehicles with the specified make
     */
    public List<Vehicle> getByMake(String make) {
        return findBy("make", make);
    }
    
    /**
     * Get vehicles by model
     * @param model The vehicle model (e.g., "Camry")
     * @return List of vehicles with the specified model
     */
    public List<Vehicle> getByModel(String model) {
        return findBy("model", model);
    }
    
    /**
     * Get vehicles by year
     * @param year The vehicle year
     * @return List of vehicles from the specified year
     */
    public List<Vehicle> getByYear(Integer year) {
        return findBy("year", year);
    }
    
    /**
     * Get vehicles by year range
     * @param startYear Start year (inclusive)
     * @param endYear End year (inclusive)
     * @return List of vehicles within the year range
     */
    public List<Vehicle> getByYearRange(Integer startYear, Integer endYear) {
        return getByRange("year", startYear, endYear);
    }
    
    /**
     * Search vehicles by make, model, or license plate
     * @param searchTerm The search term
     * @return List of matching vehicles
     */
    public List<Vehicle> searchVehicles(String searchTerm) {
        return searchMultiple(searchTerm, "make", "model", "license_plate");
    }
    
    /**
     * Register a new vehicle for a client
     * @param vehicle The vehicle to register
     * @return The created vehicle with generated ID
     * @throws IllegalArgumentException if license plate already exists
     */
    public Vehicle registerVehicle(Vehicle vehicle) {
        if (vehicle.getLicensePlate() != null && 
            !vehicle.getLicensePlate().isEmpty() && 
            licensePlateExists(vehicle.getLicensePlate())) {
            throw new IllegalArgumentException("License plate already exists");
        }
        
        return create(vehicle);
    }
}

