package com.example.programminggroupproject.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class ServiceRequest {

    private Long id;
    private Long clientId;
    private Long vehicleId;
    private Long shopId;
    private Long mechanicId;
    private String status;
    private BigDecimal totalPriceEstimated;
    private BigDecimal totalPriceFinal;
    private LocalDateTime createdAt;

    public ServiceRequest() {
    }

    public ServiceRequest(Long id, Long clientId, Long vehicleId, Long shopId, Long mechanicId,
            String status, BigDecimal totalPriceEstimated, BigDecimal totalPriceFinal,
            LocalDateTime createdAt) {
        this.id = id;
        this.clientId = clientId;
        this.vehicleId = vehicleId;
        this.shopId = shopId;
        this.mechanicId = mechanicId;
        this.status = status;
        this.totalPriceEstimated = totalPriceEstimated;
        this.totalPriceFinal = totalPriceFinal;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getClientId() {
        return clientId;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }

    public Long getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(Long vehicleId) {
        this.vehicleId = vehicleId;
    }

    public Long getShopId() {
        return shopId;
    }

    public void setShopId(Long shopId) {
        this.shopId = shopId;
    }

    public Long getMechanicId() {
        return mechanicId;
    }

    public void setMechanicId(Long mechanicId) {
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    private String clientName;
    private String vehicleInfo;
    private String serviceDescription;

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
                ", clientName='" + clientName + '\'' +
                ", vehicleInfo='" + vehicleInfo + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
