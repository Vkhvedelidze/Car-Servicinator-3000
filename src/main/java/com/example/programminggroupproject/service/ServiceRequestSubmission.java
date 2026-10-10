package com.example.programminggroupproject.service;

import com.example.programminggroupproject.model.ServiceRequest;

import java.util.Collection;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;

/** Validates and prices a request before invoking its persistence boundary. */
public final class ServiceRequestSubmission {
    private final Function<ServiceRequest, ServiceRequest> writer;

    public ServiceRequestSubmission(Function<ServiceRequest, ServiceRequest> writer) {
        this.writer = Objects.requireNonNull(writer, "writer");
    }

    public ServiceRequest submit(UUID clientId, UUID vehicleId, UUID shopId, ServiceCatalog catalog,
                                 Collection<UUID> selectedIds, String notes) {
        if (clientId == null) {
            throw new IllegalArgumentException("Sign in before submitting a service request.");
        }
        if (vehicleId == null) {
            throw new IllegalArgumentException("Select a vehicle before submitting.");
        }
        if (shopId == null) {
            throw new IllegalArgumentException("Select a shop before submitting.");
        }
        if (catalog == null) {
            throw new IllegalArgumentException("The service catalog is unavailable.");
        }
        ServiceCatalog.Quote quote = catalog.quote(selectedIds);
        ServiceRequest request = new ServiceRequest(clientId, vehicleId, shopId, null,
                "Pending", quote.total(), null, notes);
        request.setServiceDescription(quote.description());

        ServiceRequest saved = writer.apply(request);
        if (saved == null || saved.getId() == null) {
            throw new IllegalStateException("The saved request could not be confirmed. Check your requests before trying again.");
        }
        return saved;
    }
}
