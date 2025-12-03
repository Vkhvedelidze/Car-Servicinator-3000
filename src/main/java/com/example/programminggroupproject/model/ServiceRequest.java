package com.example.programminggroupproject.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * ServiceRequest model matching the Supabase service_requests table schema.
 * Schema: id (UUID), client_id, vehicle_id, shop_id, mechanic_id, status,
 * total_price_estimated, total_price_final, created_at
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
public class ServiceRequest {

    @JsonProperty("id")
    private UUID id;

    @JsonProperty("client_id")
    private UUID clientId;

    @JsonProperty("vehicle_id")
    private UUID vehicleId;

    @JsonProperty("shop_id")
    private UUID shopId;

    @JsonProperty("mechanic_id")
    private UUID mechanicId;

    @JsonProperty("status")
    private String status;

    @JsonProperty("total_price_estimated")
    private BigDecimal totalPriceEstimated;

    @JsonProperty("total_price_final")
    private BigDecimal totalPriceFinal;

    @JsonProperty("created_at")
    private OffsetDateTime createdAt;
    
    @JsonProperty("notes")
    private String notes;

    // Additional helper fields (not in database, for display purposes)
    @JsonProperty("full_name")
    private String clientName;
    private String vehicleInfo;

    @JsonProperty("service_description")
    private String serviceDescription;

    // Default constructor for Jackson
    public ServiceRequest() {
    }

    // Constructor for creating new service requests (without ID and created_at)
    public ServiceRequest(UUID clientId, UUID vehicleId, UUID shopId, UUID mechanicId, String status,
                          BigDecimal totalPriceEstimated, BigDecimal totalPriceFinal, String notes) {
        this.clientId = clientId;
        this.vehicleId = vehicleId;
        this.shopId = shopId;
        this.mechanicId = mechanicId;
        this.status = status;
        this.totalPriceEstimated = totalPriceEstimated;
        this.totalPriceFinal = totalPriceFinal;
        this.notes = notes;
    }

    // Full constructor
    public ServiceRequest(UUID id, UUID clientId, UUID vehicleId, UUID shopId, UUID mechanicId,
                          String status, BigDecimal totalPriceEstimated, BigDecimal totalPriceFinal,
                          OffsetDateTime createdAt, String notes) {
        this.id = id;
        this.clientId = clientId;
        this.vehicleId = vehicleId;
        this.shopId = shopId;
        this.mechanicId = mechanicId;
        this.status = status;
        this.totalPriceEstimated = totalPriceEstimated;
        this.totalPriceFinal = totalPriceFinal;
        this.createdAt = createdAt;
        this.notes = notes;
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getClientId() {
        return clientId;
    }

    public void setClientId(UUID clientId) {
        this.clientId = clientId;
    }

    public UUID getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(UUID vehicleId) {
        this.vehicleId = vehicleId;
    }

    public UUID getShopId() {
        return shopId;
    }

    public void setShopId(UUID shopId) {
        this.shopId = shopId;
    }

    public UUID getMechanicId() {
        return mechanicId;
    }

    public void setMechanicId(UUID mechanicId) {
        this.mechanicId = mechanicId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public BigDecimal getTotalPriceEstimated() {
        return totalPriceEstimated;
    }

    public void setTotalPriceEstimated(BigDecimal totalPriceEstimated) {
        this.totalPriceEstimated = totalPriceEstimated;
    }

    public BigDecimal getTotalPriceFinal() {
        return totalPriceFinal;
    }

    public void setTotalPriceFinal(BigDecimal totalPriceFinal) {
        this.totalPriceFinal = totalPriceFinal;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }

    // Helper fields getters/setters
    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getVehicleInfo() {
        return vehicleInfo;
    }

    public void setVehicleInfo(String vehicleInfo) {
        this.vehicleInfo = vehicleInfo;
    }

    public String getServiceDescription() {
        return serviceDescription;
    }

    public void setServiceDescription(String serviceDescription) {
        this.serviceDescription = serviceDescription;
    }

    @Override
    public String toString() {
        return "ServiceRequest{" +
                "id=" + id +
                ", clientId=" + clientId +
                ", vehicleId=" + vehicleId +
                ", shopId=" + shopId +
                ", mechanicId=" + mechanicId +
                ", status='" + status + '\'' +
                ", totalPriceEstimated=" + totalPriceEstimated +
                ", totalPriceFinal=" + totalPriceFinal +
                ", createdAt=" + createdAt +
                ", notes='" + notes + '\'' +
                ", clientName='" + clientName + '\'' +
                ", vehicleInfo='" + vehicleInfo + '\'' +
                '}';
    }
}
