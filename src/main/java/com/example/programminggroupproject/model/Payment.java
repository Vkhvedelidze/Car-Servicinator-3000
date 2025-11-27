package com.example.programminggroupproject.model;

import java.time.Instant;
import java.util.Date;

public class Payment {
    private int id;
    private int serviceRequestId;
    private double amount;
    private PaymentStatus status;
    private Instant createdAt;

    Payment() {}

    // ============= GETTERS =============
    public int getId() { return id; }
    public int getServiceRequestId() { return serviceRequestId; }
    public double getAmount() { return amount; }
    public PaymentStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }

    // ============= SETTERS =============
    
    public void setId(int id) { this.id = id; }
    public void setServiceRequestId(int serviceRequestId) { this.serviceRequestId = serviceRequestId; }
    public void setAmount(double amount) { this.amount = amount; }
    public void setStatus(PaymentStatus status) { this.status = status; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
