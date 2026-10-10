package com.example.programminggroupproject.service;

import com.example.programminggroupproject.model.Service;
import com.example.programminggroupproject.model.ServiceRequest;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ServiceRequestSubmissionTest {
    private final UUID clientId = UUID.randomUUID();
    private final UUID vehicleId = UUID.randomUUID();
    private final UUID shopId = UUID.randomUUID();

    @Test
    void submitsOnePendingRequestWithTheExactCatalogQuote() {
        Service first = service("Oil change", "0.10");
        Service second = service("Inspection", "0.20");
        ServiceCatalog catalog = catalog(List.of(first, second));
        List<ServiceRequest> writes = new ArrayList<>();
        ServiceRequest saved = new ServiceRequest();
        saved.setId(UUID.randomUUID());
        ServiceRequestSubmission submission = new ServiceRequestSubmission(request -> {
            writes.add(request);
            return saved;
        });

        ServiceRequest result = assertDoesNotThrow(() -> submission.submit(clientId, vehicleId, shopId,
                catalog, List.of(first.getId(), second.getId()), "Check tire pressure"));

        assertSame(saved, result);
        assertEquals(1, writes.size());
        ServiceRequest request = writes.getFirst();
        assertAll(
                () -> assertEquals(clientId, request.getClientId()),
                () -> assertEquals(vehicleId, request.getVehicleId()),
                () -> assertEquals(shopId, request.getShopId()),
                () -> assertEquals("Pending", request.getStatus()),
                () -> assertEquals(new BigDecimal("0.30"), request.getTotalPriceEstimated()),
                () -> assertEquals("Oil change, Inspection", request.getServiceDescription()),
                () -> assertEquals("Check tire pressure", request.getNotes()),
                () -> assertNull(request.getId()),
                () -> assertNull(request.getMechanicId()),
                () -> assertNull(request.getTotalPriceFinal()));
    }

    @Test
    void submitsAnExplicitlyFreeService() {
        Service service = service("Courtesy check", "0.00");
        ServiceCatalog catalog = catalog(List.of(service));
        List<ServiceRequest> writes = new ArrayList<>();
        ServiceRequestSubmission submission = savingSubmission(writes);

        ServiceRequest saved = assertDoesNotThrow(() -> submission.submit(clientId, vehicleId, shopId,
                catalog, List.of(service.getId()), null));

        assertNotNull(saved.getId());
        assertEquals(1, writes.size());
        assertEquals(new BigDecimal("0.00"), writes.getFirst().getTotalPriceEstimated());
        assertNull(writes.getFirst().getNotes());
    }

    @Test
    void missingRequiredContextNeverReachesPersistence() {
        Service service = service("Inspection", "20.00");
        ServiceCatalog catalog = catalog(List.of(service));
        List<ServiceRequest> writes = new ArrayList<>();
        ServiceRequestSubmission submission = savingSubmission(writes);
        List<UUID> selectedIds = List.of(service.getId());

        assertAll(
                () -> assertThrows(IllegalArgumentException.class,
                        () -> submission.submit(null, vehicleId, shopId, catalog, selectedIds, "")),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> submission.submit(clientId, null, shopId, catalog, selectedIds, "")),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> submission.submit(clientId, vehicleId, null, catalog, selectedIds, "")),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> submission.submit(clientId, vehicleId, shopId, null, selectedIds, "")));
        assertTrue(writes.isEmpty());
    }

    @Test
    void invalidSelectionNeverReachesPersistence() {
        Service service = service("Inspection", "20.00");
        ServiceCatalog catalog = catalog(List.of(service));
        List<ServiceRequest> writes = new ArrayList<>();
        ServiceRequestSubmission submission = savingSubmission(writes);
        List<Collection<UUID>> invalidSelections = Arrays.asList(null, List.of(), List.of(UUID.randomUUID()),
                List.of(service.getId(), service.getId()), Arrays.asList((UUID) null));

        for (Collection<UUID> selection : invalidSelections) {
            assertThrows(IllegalArgumentException.class,
                    () -> submission.submit(clientId, vehicleId, shopId, catalog, selection, ""));
        }
        assertTrue(writes.isEmpty());
    }

    @Test
    void unavailablePricesNeverReachPersistence() {
        List<ServiceRequest> writes = new ArrayList<>();
        ServiceRequestSubmission submission = savingSubmission(writes);

        for (String price : Arrays.asList(null, "-1.00", "3.141")) {
            Service service = service("Inspection", price);
            ServiceCatalog catalog = catalog(List.of(service));

            assertThrows(IllegalArgumentException.class, () -> submission.submit(clientId, vehicleId, shopId,
                    catalog, List.of(service.getId()), ""));
        }
        assertTrue(writes.isEmpty());
    }

    @Test
    void missingSavedRecordOrSavedIdCannotReportSuccess() {
        Service service = service("Inspection", "20.00");
        ServiceCatalog catalog = catalog(List.of(service));

        for (ServiceRequest saved : Arrays.asList(null, new ServiceRequest())) {
            List<ServiceRequest> writes = new ArrayList<>();
            ServiceRequestSubmission submission = new ServiceRequestSubmission(request -> {
                writes.add(request);
                return saved;
            });

            assertThrows(IllegalStateException.class, () -> submission.submit(clientId, vehicleId, shopId,
                    catalog, List.of(service.getId()), ""));
            assertEquals(1, writes.size());
        }
    }

    @Test
    void persistenceFailurePropagatesWithoutAnotherWrite() {
        Service service = service("Inspection", "20.00");
        ServiceCatalog catalog = catalog(List.of(service));
        List<ServiceRequest> writes = new ArrayList<>();
        IllegalStateException failure = new IllegalStateException("Cannot save request");
        ServiceRequestSubmission submission = new ServiceRequestSubmission(request -> {
            writes.add(request);
            throw failure;
        });

        IllegalStateException thrown = assertThrows(IllegalStateException.class,
                () -> submission.submit(clientId, vehicleId, shopId, catalog, List.of(service.getId()), ""));

        assertSame(failure, thrown);
        assertEquals(1, writes.size());
    }

    private static ServiceRequestSubmission savingSubmission(List<ServiceRequest> writes) {
        return new ServiceRequestSubmission(request -> {
            writes.add(request);
            request.setId(UUID.randomUUID());
            return request;
        });
    }

    private static ServiceCatalog catalog(List<Service> services) {
        ServiceCatalog catalog = assertDoesNotThrow(() -> ServiceCatalog.from(services));
        assertNotNull(catalog);
        return catalog;
    }

    private static Service service(String name, String price) {
        return new Service(UUID.randomUUID(), name, "Check vehicle",
                price == null ? null : new BigDecimal(price), null);
    }
}
