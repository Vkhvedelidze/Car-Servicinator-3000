package com.example.programminggroupproject.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ServiceRequestJsonTest {
    @Test
    void newRequestUsesDatabaseFieldNamesAndKeepsExactEstimate() throws Exception {
        ServiceRequest request = new ServiceRequest();
        UUID clientId = UUID.randomUUID();
        request.setClientId(clientId);
        request.setVehicleId(UUID.randomUUID());
        request.setShopId(UUID.randomUUID());
        request.setStatus("Pending");
        request.setTotalPriceEstimated(new BigDecimal("123.45"));
        request.setServiceDescription("Oil change, Inspection");

        JsonNode json = new ObjectMapper().readTree(new ObjectMapper().writeValueAsString(request));

        assertEquals(clientId.toString(), json.get("client_id").asText());
        assertEquals("Pending", json.get("status").asText());
        assertEquals(new BigDecimal("123.45"), json.get("total_price_estimated").decimalValue());
        assertEquals("Oil change, Inspection", json.get("service_description").asText());
        assertFalse(json.has("totalPriceEstimated"));
        assertFalse(json.has("id"), "The database supplies the new request ID");
    }
}
