package com.example.programminggroupproject.service;

import com.example.programminggroupproject.model.ServiceStatusUpdate;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.List;
import java.util.UUID;

/**
 * Service for managing ServiceStatusUpdate entities with Supabase backend.
 * Provides status update-specific operations in addition to standard CRUD.
 */
public class ServiceStatusUpdateService extends BaseSupabaseService<ServiceStatusUpdate> {
    
    private static ServiceStatusUpdateService instance;
    
    private ServiceStatusUpdateService() {
        super("service_status_updates", ServiceStatusUpdate.class, new TypeReference<List<ServiceStatusUpdate>>() {});
    }
    
    /**
     * Get singleton instance of ServiceStatusUpdateService
     */
    public static synchronized ServiceStatusUpdateService getInstance() {
        if (instance == null) {
            instance = new ServiceStatusUpdateService();
        }
        return instance;
    }
    
    // ==================== SERVICE STATUS UPDATE-SPECIFIC OPERATIONS ====================
    
    /**
     * Get all status updates for a specific service request
     * @param serviceRequestId The service request ID (UUID)
     * @return List of status updates ordered by creation time
     */
    public List<ServiceStatusUpdate> getByServiceRequestId(UUID serviceRequestId) {
        return filterAndOrder("service_request_id", "eq", serviceRequestId, "created_at", false);
    }
    
    /**
     * Get status updates by status
     * @param status The status to filter by (e.g., "Pending", "In Progress", "Completed")
     * @return List of status updates with the specified status
     */
    public List<ServiceStatusUpdate> getByStatus(String status) {
        return findBy("status", status);
    }
    
    /**
     * Get status updates created by a specific user
     * @param userId The user ID (UUID) who created the updates
     * @return List of status updates created by the user
     */
    public List<ServiceStatusUpdate> getByCreatedBy(UUID userId) {
        return findBy("created_by", userId);
    }
    
    /**
     * Get the most recent status update for a service request
     * @param serviceRequestId The service request ID (UUID)
     * @return List with the most recent status update (or empty if none)
     */
    public List<ServiceStatusUpdate> getLatestUpdate(UUID serviceRequestId) {
        return filterAndOrder("service_request_id", "eq", serviceRequestId, "created_at", false)
                .stream()
                .limit(1)
                .toList();
    }
    
    /**
     * Create a new status update for a service request
     * @param serviceRequestId The service request ID (UUID)
     * @param status The new status
     * @param note Optional note about the status change
     * @param createdBy User ID who created the update (UUID)
     * @return The created status update
     */
    public ServiceStatusUpdate createStatusUpdate(UUID serviceRequestId, String status, 
                                                   String note, UUID createdBy) {
        ServiceStatusUpdate update = new ServiceStatusUpdate(serviceRequestId, status, note, createdBy);
        return create(update);
    }
    
    /**
     * Search status updates by note content
     * @param searchTerm The search term
     * @return List of matching status updates
     */
    public List<ServiceStatusUpdate> searchByNote(String searchTerm) {
        return search("note", searchTerm);
    }
}

