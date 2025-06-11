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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    @JsonIgnore
    private User patient;

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "payment_date")
    private LocalDateTime paymentDate;

    @Column(name = "payment_method", length = 255)
    private String paymentMethod;

    @Column(name = "status", length = 50)
    @Builder.Default
    private String status = "Chờ thanh toán";

    @Column(name = "transaction_id", length = 100)
    private String transactionId;

    @Column(name = "transaction_ref", length = 100)
    private String transactionRef;

    @Column(name = "transaction_date", length = 50)
    private String transactionDate;

    @Column(name = "bank_code", length = 20)
    private String bankCode;

    @Column(name = "card_type", length = 20)
    private String cardType;

    @Column(name = "response_code", length = 10)
    private String responseCode;

    @Column(name = "secure_hash", length = 255)
    private String secureHash;

    @Column(name = "invoice_number", length = 50)
    private String invoiceNumber;

    @Column(name = "notes", length = 500)
    private String notes;

    @Column(name = "invoice_generated", nullable = false)
    @Builder.Default
    private Boolean invoiceGenerated = false;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "payment", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private Set<PaymentOrder> paymentOrders = new HashSet<>();

    // Helper method to add payment order
    public void addPaymentOrder(PaymentOrder paymentOrder) {
        paymentOrders.add(paymentOrder);
        paymentOrder.setPayment(this);
    }

    // Helper method to remove payment order
    public void removePaymentOrder(PaymentOrder paymentOrder) {
        paymentOrders.remove(paymentOrder);
        paymentOrder.setPayment(null);
    }

    // Helper method to get lab test order IDs
    public List<Long> getLabTestOrderIds() {
        if (paymentOrders == null || paymentOrders.isEmpty()) {
            return new ArrayList<>();
        }
        return paymentOrders.stream()
                .map(po -> po.getLabTestOrder().getId())
                .collect(Collectors.toList());
    }
}
