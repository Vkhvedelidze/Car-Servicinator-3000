package com.example.programminggroupproject.model;

import java.math.BigDecimal;

public class ServiceRequestItem {

        private Long id;
        private Long serviceRequestId;
        private Long serviceId;
        private int quantity;
        private BigDecimal priceEstimated;
        private BigDecimal priceFinal;
        private String source;
        private boolean isApproved;

        public ServiceRequestItem() {}

        public ServiceRequestItem(Long id, Long serviceRequestId, Long serviceId, int quantity,
                                  BigDecimal priceEstimated, BigDecimal priceFinal,
                                  String source, boolean isApproved) {
            this.id = id;
            this.serviceRequestId = serviceRequestId;
            this.serviceId = serviceId;
            this.quantity = quantity;
            this.priceEstimated = priceEstimated;
            this.priceFinal = priceFinal;
            this.source = source;
            this.isApproved = isApproved;
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

        public Long getServiceId() {
            return serviceId;
        }

        public void setServiceId(Long serviceId) {
            this.serviceId = serviceId;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }

        public BigDecimal getPriceEstimated() {
            return priceEstimated;
        }

        public void setPriceEstimated(BigDecimal priceEstimated) {
            this.priceEstimated = priceEstimated;
        }

        public BigDecimal getPriceFinal() {
            return priceFinal;
        }

        public void setPriceFinal(BigDecimal priceFinal) {
            this.priceFinal = priceFinal;
        }

        public String getSource() {
            return source;
        }

        public void setSource(String source) {
            this.source = source;
        }

        public boolean isApproved() {
            return isApproved;
        }

        public void setApproved(boolean approved) {
            isApproved = approved;
        }


        public BigDecimal getTotalEstimated() {
            if (priceEstimated == null) return null;
            return priceEstimated.multiply(BigDecimal.valueOf(quantity));
        }


        public BigDecimal getTotalFinal() {
            if (priceFinal == null) return null;
            return priceFinal.multiply(BigDecimal.valueOf(quantity));
        }

        @Override
        public String toString() {
            return "ServiceRequestItem{" +
                    "id=" + id +
                    ", serviceRequestId=" + serviceRequestId +
                    ", serviceId=" + serviceId +
                    ", quantity=" + quantity +
                    ", priceEstimated=" + priceEstimated +
                    ", priceFinal=" + priceFinal +
                    ", source='" + source + '\'' +
                    ", isApproved=" + isApproved +
                    '}';
        }
}
