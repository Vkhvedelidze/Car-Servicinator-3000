package com.example.programminggroupproject.service;

import com.example.programminggroupproject.model.Service;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ServiceCatalogTest {
    @Test
    void sumsSelectedBackendPricesUsingExactDecimals() {
        Service first = service("Oil change", "0.10");
        Service second = service("Inspection", "0.20");
        Service unselected = service("Engine repair", "900.00");
        ServiceCatalog catalog = catalog(List.of(first, second, unselected));

        ServiceCatalog.Quote quote = assertDoesNotThrow(
                () -> catalog.quote(List.of(first.getId(), second.getId())));

        assertEquals(new BigDecimal("0.30"), quote.total());
        assertEquals("Oil change, Inspection", quote.description());
        assertEquals(List.of(first.getId(), second.getId()),
                quote.entries().stream().map(ServiceCatalog.Entry::id).toList());
    }

    @Test
    void retainsDuplicateNamesAsSeparateServicesById() {
        Service first = service("Inspection", "20.00");
        Service second = service("Inspection", "35.00");
        ServiceCatalog catalog = catalog(List.of(first, second));

        assertEquals(2, catalog.entries().size());
        ServiceCatalog.Quote quote = assertDoesNotThrow(
                () -> catalog.quote(List.of(second.getId(), first.getId())));

        assertEquals(new BigDecimal("55.00"), quote.total());
        assertEquals(List.of(second.getId(), first.getId()),
                quote.entries().stream().map(ServiceCatalog.Entry::id).toList());
        assertEquals("Inspection, Inspection", quote.description());
    }

    @Test
    void explicitZeroPriceIsAValidFreeService() {
        Service free = service("Courtesy check", "0");
        ServiceCatalog catalog = catalog(List.of(free));

        assertEquals(1, catalog.entries().size());
        assertTrue(catalog.entries().getFirst().available());
        ServiceCatalog.Quote quote = assertDoesNotThrow(() -> catalog.quote(List.of(free.getId())));
        assertEquals(new BigDecimal("0.00"), quote.total());
    }

    @Test
    void acceptsAdditionalDecimalPlacesOnlyWhenTheyAreTrailingZeros() {
        Service service = service("Inspection", "12.3400");
        ServiceCatalog catalog = catalog(List.of(service));

        assertEquals(1, catalog.entries().size());
        assertTrue(catalog.entries().getFirst().available());
        ServiceCatalog.Quote quote = assertDoesNotThrow(() -> catalog.quote(List.of(service.getId())));
        assertEquals(new BigDecimal("12.34"), quote.total());
    }

    @Test
    void unavailablePricesStayVisibleButCannotBeQuoted() {
        for (BigDecimal price : Arrays.asList(null, new BigDecimal("-0.01"), new BigDecimal("0.001"))) {
            Service service = new Service(UUID.randomUUID(), "Inspection", "Check vehicle", price, null);
            ServiceCatalog catalog = catalog(List.of(service));

            assertEquals(1, catalog.entries().size());
            assertFalse(catalog.entries().getFirst().available(), "Invalid price: " + price);
            assertThrows(IllegalArgumentException.class, () -> catalog.quote(List.of(service.getId())));
        }
    }

    @Test
    void unavailableUnselectedServiceDoesNotBlockValidSelection() {
        Service available = service("Oil change", "49.99");
        Service unavailable = service("Inspection", null);
        ServiceCatalog catalog = catalog(List.of(available, unavailable));

        ServiceCatalog.Quote quote = assertDoesNotThrow(() -> catalog.quote(List.of(available.getId())));

        assertEquals(new BigDecimal("49.99"), quote.total());
    }

    @Test
    void rejectsMissingOrEmptySelection() {
        ServiceCatalog catalog = catalog(List.of(service("Oil change", "49.99")));

        assertThrows(IllegalArgumentException.class, () -> catalog.quote(null));
        assertThrows(IllegalArgumentException.class, () -> catalog.quote(List.of()));
    }

    @Test
    void rejectsUnknownNullAndRepeatedSelectionIds() {
        Service service = service("Oil change", "49.99");
        ServiceCatalog catalog = catalog(List.of(service));

        assertThrows(IllegalArgumentException.class, () -> catalog.quote(List.of(UUID.randomUUID())));
        assertThrows(IllegalArgumentException.class, () -> catalog.quote(Arrays.asList((UUID) null)));
        assertThrows(IllegalArgumentException.class, () -> catalog.quote(List.of(service.getId(), service.getId())));
    }

    @Test
    void rejectsDuplicateCatalogIdsEvenWhenNamesDiffer() {
        Service first = service("Inspection", "20.00");
        Service second = service("Oil change", "35.00");
        second.setId(first.getId());

        assertThrows(IllegalArgumentException.class, () -> ServiceCatalog.from(List.of(first, second)));
    }

    @Test
    void rejectsMalformedCatalogIdentityInsteadOfSkippingRecords() {
        assertThrows(IllegalArgumentException.class, () -> ServiceCatalog.from(null));
        assertThrows(IllegalArgumentException.class, () -> ServiceCatalog.from(Arrays.asList((Service) null)));
        Service missingId = service("Inspection", "20.00");
        missingId.setId(null);
        assertThrows(IllegalArgumentException.class, () -> ServiceCatalog.from(List.of(missingId)));
        for (String name : Arrays.asList(null, "", " \t")) {
            Service malformed = service(name, "20.00");
            assertThrows(IllegalArgumentException.class, () -> ServiceCatalog.from(List.of(malformed)));
        }
    }

    @Test
    void acceptsEmptyCatalogButCannotQuoteIt() {
        ServiceCatalog catalog = catalog(List.of());

        assertTrue(catalog.entries().isEmpty());
        assertThrows(IllegalArgumentException.class, () -> catalog.quote(List.of(UUID.randomUUID())));
    }

    @Test
    void snapshotsMutableBackendRowsAndSourceList() {
        Service service = service("Oil change", "49.99");
        UUID originalId = service.getId();
        List<Service> source = new ArrayList<>(List.of(service));
        ServiceCatalog catalog = catalog(source);

        service.setId(UUID.randomUUID());
        service.setName("Changed name");
        service.setDescription("Changed description");
        service.setBasePrice(new BigDecimal("999.00"));
        source.clear();

        ServiceCatalog.Quote quote = assertDoesNotThrow(() -> catalog.quote(List.of(originalId)));
        assertEquals(new BigDecimal("49.99"), quote.total());
        assertEquals("Oil change", quote.description());
        assertEquals("Check vehicle", quote.entries().getFirst().description());
    }

    @Test
    void catalogAndQuoteEntryListsCannotBeMutated() {
        Service service = service("Inspection", "20.00");
        ServiceCatalog catalog = catalog(List.of(service));
        ServiceCatalog.Quote quote = assertDoesNotThrow(() -> catalog.quote(List.of(service.getId())));

        assertThrows(UnsupportedOperationException.class, () -> catalog.entries().clear());
        assertThrows(UnsupportedOperationException.class, () -> quote.entries().clear());
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
