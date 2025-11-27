package com.example.programminggroupproject.model;

import java.time.LocalDateTime;

public class ServiceStatusUpdate {

    private Long id;
    private Long serviceRequestId;
    private String status;
    private String note;
    private Long createdBy;
    private LocalDateTime createdAt;

    public ServiceStatusUpdate() {}

    public ServiceStatusUpdate(Long id, Long serviceRequestId, String status, String note, Long createdBy, LocalDateTime createdAt) {
        this.id = id;
        this.serviceRequestId = serviceRequestId;
        this.status = status;
        this.note = note;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getServiceRequestId() {
        return serviceRequestId;
    }

    public void setServiceRequestId(Long serviceRequestId) {
        this.serviceRequestId = serviceRequestId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "ServiceStatusUpdate{" +
                "id=" + id +
                ", serviceRequestId=" + serviceRequestId +
                ", status='" + status + '\'' +
                ", note='" + note + '\'' +
                ", createdBy=" + createdBy +
                ", createdAt=" + createdAt +
                '}';
    }
}
