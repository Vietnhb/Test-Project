package com.fpt.hivtreatment.service.impl;

import com.fpt.hivtreatment.dto.PaymentDTO;
import com.fpt.hivtreatment.dto.ProcessPaymentRequest;
import com.fpt.hivtreatment.exception.ResourceNotFoundException;
import com.fpt.hivtreatment.model.entity.LabTestOrder;
import com.fpt.hivtreatment.model.entity.LabTestResult;
import com.fpt.hivtreatment.model.entity.Payment;
import com.fpt.hivtreatment.model.entity.PaymentOrder;
import com.fpt.hivtreatment.model.entity.User;
import com.fpt.hivtreatment.repository.LabTestOrderRepository;
import com.fpt.hivtreatment.repository.LabTestResultRepository;
import com.fpt.hivtreatment.repository.PaymentOrderRepository;
import com.fpt.hivtreatment.repository.PaymentRepository;
import com.fpt.hivtreatment.repository.UserRepository;
import com.fpt.hivtreatment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentOrderRepository paymentOrderRepository;
    private final LabTestOrderRepository labTestOrderRepository;
    private final UserRepository userRepository;
    private final LabTestResultRepository labTestResultRepository;

    @Override
    public Payment createPayment(Long patientId, List<Long> labTestOrderIds, String paymentMethod) {
        log.info("Creating payment for patient ID: {} with {} lab test orders", patientId, labTestOrderIds.size());

        // Find patient
        User patient = userRepository.findById(patientId)
                .orElseThrow(() -> {
                    log.error("Patient not found with ID: {}", patientId);
                    return new ResourceNotFoundException("Patient not found with ID: " + patientId);
                });

        // Validate lab test orders
        if (labTestOrderIds == null || labTestOrderIds.isEmpty()) {
            log.error("No lab test order IDs provided");
            throw new IllegalArgumentException("At least one lab test order ID is required");
        }

        // Check for duplicate or already paid orders
        for (Long orderId : labTestOrderIds) {
            if (paymentOrderRepository.existsByLabTestOrderId(orderId)) {
                log.error("Lab test order with ID {} has already been paid", orderId);
                throw new IllegalStateException("Lab test order with ID " + orderId + " has already been paid");
            }
        }

        // Calculate total amount
        BigDecimal totalAmount = calculateTotalAmount(labTestOrderIds);
        log.info("Total amount calculated: {}", totalAmount);

        // Create payment - removing paymentMethod and paymentDate requirements
        Payment payment = Payment.builder()
                .patient(patient)
                .totalAmount(totalAmount)
                .status("Chờ thanh toán")
                .build();

        // Save payment to get ID
        payment = paymentRepository.save(payment);
        log.info("Payment saved with ID: {}", payment.getId());

        // Create payment orders for each lab test order
        List<LabTestOrder> labTestOrders = labTestOrderRepository.findAllById(labTestOrderIds);

        if (labTestOrders.size() != labTestOrderIds.size()) {
            log.error("Not all lab test orders were found. Found: {}, Expected: {}",
                    labTestOrders.size(), labTestOrderIds.size());
            throw new ResourceNotFoundException("One or more lab test orders not found");
        }

        List<PaymentOrder> paymentOrders = new ArrayList<>();
        for (LabTestOrder labTestOrder : labTestOrders) {
            PaymentOrder paymentOrder = PaymentOrder.builder()
                    .payment(payment)
                    .labTestOrder(labTestOrder)
                    .build();

            paymentOrders.add(paymentOrder);

            // Update lab test order status
            labTestOrder.setStatus("Chờ lấy mẫu");
            labTestOrderRepository.save(labTestOrder);
        }

        // Save all payment orders in one batch
        paymentOrderRepository.saveAll(paymentOrders);
        log.info("Created and saved {} payment order records", paymentOrders.size());

        // Return the payment with updated information
        return paymentRepository.findById(payment.getId()).orElse(payment);
    }

    @Override
    public Payment getPaymentById(Long paymentId) {
        log.info("Fetching payment with ID: {}", paymentId);
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> {
                    log.error("Payment not found with ID: {}", paymentId);
                    return new ResourceNotFoundException("Payment not found with ID: " + paymentId);
                });
    }

    @Override
    public List<Payment> getPaymentsByPatientId(Long patientId) {
        log.info("Fetching payments for patient ID: {}", patientId);
        return paymentRepository.findByPatientIdOrderByCreatedAtDesc(patientId);
    }

    @Override
    public Payment updatePaymentStatus(Long paymentId, String status) {
        log.info("Updating payment status for ID: {} to {}", paymentId, status);
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> {
                    log.error("Payment not found with ID: {}", paymentId);
                    return new ResourceNotFoundException("Payment not found with ID: " + paymentId);
                });

        payment.setStatus(status);

        // If payment is completed, update lab test order statuses
        if ("Đã thanh toán".equals(status)) {
            log.info("Payment {} marked as completed, updating lab test order statuses", paymentId);
            List<PaymentOrder> paymentOrders = paymentOrderRepository.findByPaymentIdWithLabTestOrders(paymentId);

            for (PaymentOrder paymentOrder : paymentOrders) {
                LabTestOrder labTestOrder = paymentOrder.getLabTestOrder();
                if ("Chờ thanh toán".equals(labTestOrder.getStatus())) {
                    labTestOrder.setStatus("Chờ lấy mẫu");
                    labTestOrderRepository.save(labTestOrder);
                    log.debug("Updated lab test order {} status to 'Chờ lấy mẫu'", labTestOrder.getId());
                }
            }
        }

        return paymentRepository.save(payment);
    }

    @Override
    public Payment updatePaymentTransaction(Long paymentId, Map<String, String> transactionData) {
        log.info("Updating payment transaction data for ID: {}", paymentId);
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> {
                    log.error("Payment not found with ID: {}", paymentId);
                    return new ResourceNotFoundException("Payment not found with ID: " + paymentId);
                });

        if (transactionData.containsKey("transaction_id")) {
            payment.setTransactionId(transactionData.get("transaction_id"));
        }

        if (transactionData.containsKey("transaction_ref")) {
            payment.setTransactionRef(transactionData.get("transaction_ref"));
        }

        if (transactionData.containsKey("transaction_date")) {
            payment.setTransactionDate(transactionData.get("transaction_date"));
        }

        if (transactionData.containsKey("bank_code")) {
            payment.setBankCode(transactionData.get("bank_code"));
        }

        if (transactionData.containsKey("card_type")) {
            payment.setCardType(transactionData.get("card_type"));
        }

        if (transactionData.containsKey("response_code")) {
            payment.setResponseCode(transactionData.get("response_code"));
        }

        if (transactionData.containsKey("secure_hash")) {
            payment.setSecureHash(transactionData.get("secure_hash"));
        }

        if (transactionData.containsKey("status")) {
            payment.setStatus(transactionData.get("status"));
        }

        log.info("Transaction data updated for payment {}", paymentId);
        return paymentRepository.save(payment);
    }

    @Override
    public BigDecimal calculateTotalAmount(List<Long> labTestOrderIds) {
        BigDecimal totalAmount = BigDecimal.ZERO;

        List<LabTestOrder> labTestOrders = labTestOrderRepository.findAllById(labTestOrderIds);

        for (LabTestOrder labTestOrder : labTestOrders) {
            totalAmount = totalAmount.add(labTestOrder.getTestType().getPrice());
        }

        return totalAmount;
    }

    @Override
    public PaymentDTO convertToDTO(Payment payment) {
        // Use findByPaymentIdWithLabTestOrders to fetch lab test orders with EAGER
        // loading
        List<PaymentOrder> paymentOrders = paymentOrderRepository.findByPaymentIdWithLabTestOrders(payment.getId());
        if (paymentOrders.isEmpty()) {
            // Fallback to regular fetch if the specific method fails
            paymentOrders = paymentOrderRepository.findByPayment(payment);
            log.warn("Had to fall back to findByPayment for payment ID: {}", payment.getId());
        }

        List<Long> labTestOrderIds = paymentOrders.stream()
                .map(po -> po.getLabTestOrder().getId())
                .collect(Collectors.toList());

        // Add detailed information about each lab test order
        List<Map<String, Object>> labTestOrderDetails = new ArrayList<>();

        for (PaymentOrder po : paymentOrders) {
            try {
                LabTestOrder labTestOrder = po.getLabTestOrder();

                if (labTestOrder != null) {
                    Map<String, Object> orderDetail = new HashMap<>();
                    orderDetail.put("id", labTestOrder.getId());
                    orderDetail.put("status", labTestOrder.getStatus());
                    orderDetail.put("orderDate", labTestOrder.getOrderDate());
                    orderDetail.put("resultExpectedDate", labTestOrder.getResultExpectedDate());
                    orderDetail.put("notes", labTestOrder.getNotes());

                    // Add test type details if available
                    if (labTestOrder.getTestType() != null) {
                        orderDetail.put("testTypeId", labTestOrder.getTestType().getId());
                        orderDetail.put("testTypeName", labTestOrder.getTestType().getName());
                        orderDetail.put("testTypeDescription", labTestOrder.getTestType().getDescription());
                        orderDetail.put("testTypePrice", labTestOrder.getTestType().getPrice());
                    } else {
                        log.warn("TestType is null for LabTestOrder ID: {}", labTestOrder.getId());
                    }

                    labTestOrderDetails.add(orderDetail);
                } else {
                    log.warn("LabTestOrder is null for PaymentOrder ID: {}", po.getId());
                }
            } catch (Exception e) {
                log.error("Error processing lab test order for payment {}: {}", payment.getId(), e.getMessage());
            }
        }

        log.info("Converted payment {} with {} lab test orders and {} details",
                payment.getId(), labTestOrderIds.size(), labTestOrderDetails.size());

        return PaymentDTO.builder()
                .id(payment.getId())
                .patientId(payment.getPatient().getId())
                .patientName(payment.getPatient().getFullName())
                .amount(payment.getTotalAmount())
                .paymentDate(payment.getPaymentDate())
                .paymentMethod(payment.getPaymentMethod())
                .status(payment.getStatus())
                .transactionId(payment.getTransactionId())
                .transactionRef(payment.getTransactionRef())
                .transactionDate(payment.getTransactionDate())
                .bankCode(payment.getBankCode())
                .cardType(payment.getCardType())
                .responseCode(payment.getResponseCode())
                .secureHash(payment.getSecureHash())
                .createdAt(payment.getCreatedAt())
                .labTestOrderIds(labTestOrderIds)
                .labTestOrderDetails(labTestOrderDetails)
                .notes(payment.getNotes())
                .build();
    }

    @Override
    public List<Payment> getAllPayments() {
        log.info("Fetching all payments in the system");
        return paymentRepository.findAll();
    }

    @Override
    @Transactional
    public Payment processPayment(ProcessPaymentRequest request) {
        log.info("Processing payment for ID: {} with {} payment methods",
                request.getPaymentId(), request.getPaymentMethods().size());

        // Validate request
        if (request.getPaymentId() == null) {
            throw new IllegalArgumentException("Payment ID is required");
        }

        if (request.getPaymentMethods() == null || request.getPaymentMethods().isEmpty()) {
            throw new IllegalArgumentException("At least one payment method is required");
        }

        // Find payment
        Payment payment = paymentRepository.findById(request.getPaymentId())
                .orElseThrow(() -> {
                    log.error("Payment not found with ID: {}", request.getPaymentId());
                    return new ResourceNotFoundException("Payment not found with ID: " + request.getPaymentId());
                });

        // Calculate total payment amount from methods
        BigDecimal totalMethodAmount = request.getPaymentMethods().stream()
                .map(ProcessPaymentRequest.PaymentMethodEntry::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Verify total payment amount matches expected amount
        if (totalMethodAmount.compareTo(payment.getTotalAmount()) < 0) {
            throw new IllegalArgumentException("Total payment amount is less than required amount");
        }

        // Update payment with multiple payment methods
        StringBuilder paymentMethodsStr = new StringBuilder();
        for (int i = 0; i < request.getPaymentMethods().size(); i++) {
            ProcessPaymentRequest.PaymentMethodEntry entry = request.getPaymentMethods().get(i);
            if (i > 0)
                paymentMethodsStr.append(", ");
            paymentMethodsStr.append(entry.getMethod())
                    .append(" (")
                    .append(entry.getAmount())
                    .append(")");
        }

        // Update payment details
        payment.setStatus("Đã thanh toán");
        payment.setPaymentMethod(paymentMethodsStr.toString());
        payment.setPaymentDate(LocalDateTime.now());

        if (request.getNote() != null && !request.getNote().isEmpty()) {
            payment.setNotes(request.getNote());
        }

        payment = paymentRepository.save(payment);
        log.info("Payment {} marked as paid with methods: {}", payment.getId(), paymentMethodsStr);

        // Get all lab test orders associated with this payment
        List<PaymentOrder> paymentOrders = paymentOrderRepository.findByPaymentIdWithLabTestOrders(payment.getId());
        log.info("Found {} lab test orders for payment {}", paymentOrders.size(), payment.getId());

        for (PaymentOrder paymentOrder : paymentOrders) {
            LabTestOrder labTestOrder = paymentOrder.getLabTestOrder();

            // Update lab test order status
            if (labTestOrder != null) {
                if ("Chờ thanh toán".equals(labTestOrder.getStatus())) {
                    labTestOrder.setStatus("Chờ lấy mẫu");
                    labTestOrderRepository.save(labTestOrder);
                    log.debug("Updated lab test order {} status to 'Chờ lấy mẫu'", labTestOrder.getId());
                } else {
                    log.info("Lab test order {} already has status: {}",
                            labTestOrder.getId(), labTestOrder.getStatus());
                }
            }
        }

        return payment;
    }
}