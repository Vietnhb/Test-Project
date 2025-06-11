package com.fpt.hivtreatment.service;

import com.fpt.hivtreatment.dto.PaymentDTO;
import com.fpt.hivtreatment.dto.ProcessPaymentRequest;
import com.fpt.hivtreatment.model.entity.Payment;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface PaymentService {

    /**
     * Create a new payment for lab test orders
     * 
     * @param patientId       Patient making the payment
     * @param labTestOrderIds List of lab test order IDs to be paid
     * @param paymentMethod   Payment method (optional)
     * @return Created payment
     */
    Payment createPayment(Long patientId, List<Long> labTestOrderIds, String paymentMethod);

    /**
     * Get payment by ID
     * 
     * @param paymentId Payment ID
     * @return Payment if found
     */
    Payment getPaymentById(Long paymentId);

    /**
     * Get payments by patient ID
     * 
     * @param patientId Patient ID
     * @return List of payments
     */
    List<Payment> getPaymentsByPatientId(Long patientId);

    /**
     * Update payment status
     * 
     * @param paymentId Payment ID
     * @param status    New status
     * @return Updated payment
     */
    Payment updatePaymentStatus(Long paymentId, String status);

    /**
     * Update payment with transaction information
     * 
     * @param paymentId       Payment ID
     * @param transactionData Map of transaction data (transaction_id,
     *                        transaction_ref, etc.)
     * @return Updated payment
     */
    Payment updatePaymentTransaction(Long paymentId, Map<String, String> transactionData);

    /**
     * Get total amount for lab test orders
     * 
     * @param labTestOrderIds List of lab test order IDs
     * @return Total amount
     */
    BigDecimal calculateTotalAmount(List<Long> labTestOrderIds);

    /**
     * Convert entity to DTO
     * 
     * @param payment Payment entity
     * @return Payment DTO
     */
    PaymentDTO convertToDTO(Payment payment);

    /**
     * Get all payments in the system
     * 
     * @return List of all payments
     */
    List<Payment> getAllPayments();

    /**
     * Process payment with multiple payment methods
     * 
     * @param request The payment processing request with payment methods and
     *                amounts
     * @return The updated payment entity
     */
    Payment processPayment(ProcessPaymentRequest request);
}