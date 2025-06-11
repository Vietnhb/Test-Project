package com.fpt.hivtreatment.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment_order")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class PaymentOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false)
    @JsonIgnore
    private Payment payment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lab_test_order_id", nullable = false)
    @JsonIgnore
    private LabTestOrder labTestOrder;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /**
     * Get the service name from the associated lab test order
     * 
     * @return The name of the test type
     */
    public String getServiceName() {
        if (labTestOrder != null && labTestOrder.getTestType() != null) {
            return labTestOrder.getTestType().getName();
        }
        return "Dịch vụ xét nghiệm";
    }

    /**
     * Get the amount (price) from the associated lab test order
     * 
     * @return The price of the test type
     */
    public BigDecimal getAmount() {
        if (labTestOrder != null && labTestOrder.getTestType() != null) {
            return labTestOrder.getTestType().getPrice();
        }
        return BigDecimal.ZERO;
    }
}