package com.example.programminggroupproject.service;

import com.example.programminggroupproject.model.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/** Immutable snapshot of backend service identities and prices for one request form. */
public final class ServiceCatalog {
    private final List<Entry> entries;
    private final Map<UUID, Entry> entriesById;

    private ServiceCatalog(List<Entry> entries, Map<UUID, Entry> entriesById) {
        this.entries = List.copyOf(entries);
        this.entriesById = Map.copyOf(entriesById);
    }

    public static ServiceCatalog from(List<Service> services) {
        if (services == null) {
            throw new IllegalArgumentException("The service catalog could not be loaded.");
        }
        List<Entry> entries = new ArrayList<>();
        Map<UUID, Entry> entriesById = new LinkedHashMap<>();
        for (Service service : services) {
            if (service == null || service.getId() == null
                    || service.getName() == null || service.getName().isBlank()) {
                throw new IllegalArgumentException("The service catalog contains an invalid service.");
            }
            Entry entry = new Entry(service.getId(), service.getName(),
                    service.getDescription(), service.getBasePrice());
            if (entriesById.putIfAbsent(entry.id(), entry) != null) {
                throw new IllegalArgumentException("The service catalog contains a repeated service ID.");
            }
            entries.add(entry);
        }
        return new ServiceCatalog(entries, entriesById);
    }

    public List<Entry> entries() {
        return entries;
    }

    /** Quotes selected IDs using this snapshot; no price is rounded or substituted. */
    public Quote quote(Collection<UUID> selectedIds) {
        if (selectedIds == null || selectedIds.isEmpty()) {
            throw new IllegalArgumentException("Select at least one available service.");
        }
        Set<UUID> seen = new HashSet<>();
        List<Entry> selected = new ArrayList<>();
        BigDecimal total = new BigDecimal("0.00");
        for (UUID id : selectedIds) {
            Entry entry = id == null ? null : entriesById.get(id);
            if (entry == null) {
                throw new IllegalArgumentException("A selected service is no longer available. Reload the service list.");
            }
            if (!seen.add(id)) {
                throw new IllegalArgumentException("Select each service only once.");
            }
            if (!entry.available()) {
                throw new IllegalArgumentException("A selected service does not have a valid price.");
            }
            selected.add(entry);
            total = total.add(entry.price().setScale(2, RoundingMode.UNNECESSARY));
        }
        String description = selected.stream().map(Entry::name).collect(Collectors.joining(", "));
        return new Quote(selected, total, description);
    }

    public record Entry(UUID id, String name, String description, BigDecimal price) {
        /** Missing, negative, and fractional-cent prices are unavailable; zero is valid. */
        public boolean available() {
            return price != null && price.signum() >= 0 && price.stripTrailingZeros().scale() <= 2;
        }
    }

    public record Quote(List<Entry> entries, BigDecimal total, String description) {
        public Quote {
            entries = List.copyOf(entries);
        }
    }
}
