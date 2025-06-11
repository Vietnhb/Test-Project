package com.fpt.hivtreatment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO for processing payments with multiple payment methods
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessPaymentRequest {
    private Long paymentId;
    private BigDecimal amount;
    private List<PaymentMethodEntry> paymentMethods;
    private String note;
    private String status;

    /**
     * Represents a single payment method entry with its amount
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentMethodEntry {
        private String method;
        private BigDecimal amount;
    }
}