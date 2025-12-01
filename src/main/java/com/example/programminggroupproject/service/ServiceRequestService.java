package com.example.programminggroupproject.service;


import com.example.programminggroupproject.model.ServiceRequest;
import com.fasterxml.jackson.core.type.TypeReference;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Service for managing ServiceRequest entities with Supabase backend.
 * Provides service request-specific operations in addition to standard CRUD.
 */
public class ServiceRequestService extends BaseSupabaseService<ServiceRequest> {
    
    private static ServiceRequestService instance;
    
    private ServiceRequestService() {
        super("service_requests", ServiceRequest.class, new TypeReference<List<ServiceRequest>>() {});
    }
    
    /**
     * Get singleton instance of ServiceRequestService
     */
    public static synchronized ServiceRequestService getInstance() {
        if (instance == null) {
            instance = new ServiceRequestService();
        }
        return instance;
    }
    
    // ==================== SERVICE REQUEST-SPECIFIC OPERATIONS ====================
    
    /**
     * Get all service requests for a specific client
     * @param clientId The client's user ID (UUID)
     * @return List of service requests for the client
     */
    public List<ServiceRequest> getByClientId(UUID clientId) {
        return findBy("client_id", clientId);
    }
    
    /**
     * Get all service requests by client name
     * @param clientName The client's name
     * @return List of service requests for the client
     */
    public List<ServiceRequest> getByClientName(String clientName) {
        return findBy("client_name", clientName);
    }
    
    /**
     * Get all service requests assigned to a specific mechanic
     * @param mechanicId The mechanic's user ID (UUID)
     * @return List of service requests for the mechanic
     */
    public List<ServiceRequest> getByMechanicId(UUID mechanicId) {
        return findBy("mechanic_id", mechanicId);
    }
    
    /**
     * Get all service requests for a specific shop
     * @param shopId The shop ID (UUID)
     * @return List of service requests for the shop
     */
    public List<ServiceRequest> getByShopId(UUID shopId) {
        return findBy("shop_id", shopId);
    }
    
    /**
     * Get all service requests by status
     * @param status The status to filter by (e.g., "Pending", "In Progress", "Completed")
     * @return List of service requests with the specified status
     */
    public List<ServiceRequest> getByStatus(String status) {
        return findBy("status", status);
    }
    
    /**
     * Get service requests created within a date range
     * @param startDate Start date (inclusive)
     * @param endDate End date (inclusive)
     * @return List of service requests within the date range
     */
    public List<ServiceRequest> getByDateRange(OffsetDateTime startDate, OffsetDateTime endDate) {
        return getByRange("created_at", startDate, endDate);
    }
    
    /**
     * Get all pending service requests
     * @return List of pending service requests
     */
    public List<ServiceRequest> getPendingRequests() {
        return getByStatus("Pending");
    }
    
    /**
     * Get all in-progress service requests
     * @return List of in-progress service requests
     */
    public List<ServiceRequest> getInProgressRequests() {
        return getByStatus("In Progress");
    }
    
    /**
     * Get all completed service requests
     * @return List of completed service requests
     */
    public List<ServiceRequest> getCompletedRequests() {
        return getByStatus("Completed");
    }
    
    /**
     * Assign a mechanic to a service request
     * @param requestId The service request ID (UUID)
     * @param mechanicId The mechanic's user ID (UUID)
     * @return Updated service request
     */
    public ServiceRequest assignMechanic(UUID requestId, UUID mechanicId) {
        ServiceRequest request = get(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Service request not found"));
        
        request.setMechanicId(mechanicId);
        request.setStatus("In Progress");
        
        return update(requestId, request);
    }
    
    /**
     * Update service request status
     * @param requestId The service request ID (as UUID)
     * @param status The new status
     * @return Updated service request
     */
    public ServiceRequest updateStatus(UUID requestId, String status) {
        ServiceRequest request = get(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Service request not found"));
        
        request.setStatus(status);
        return update(requestId, request);
    }
    
    /**
     * Search service requests by vehicle info or service description
     * @param searchTerm The search term
     * @return List of matching service requests
     */
    public List<ServiceRequest> searchRequests(String searchTerm) {
        return searchMultiple(searchTerm, "vehicle_info", "service_description", "client_name");
    }
    
    /**
     * Get the most recent service requests
     * @param limit Maximum number of requests to return
     * @return List of recent service requests ordered by creation date (descending)
     */
    public List<ServiceRequest> getRecentRequests(int limit) {
        return filterAndOrder("id", "gte", 0, "created_at", false)
                .stream()
                .limit(limit)
                .toList();
    }
}

