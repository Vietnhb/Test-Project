package com.fpt.hivtreatment.controller;

import com.fpt.hivtreatment.dto.CreatePaymentRequest;
import com.fpt.hivtreatment.dto.PaymentDTO;
import com.fpt.hivtreatment.dto.ProcessPaymentRequest;
import com.fpt.hivtreatment.model.entity.LabTestOrder;
import com.fpt.hivtreatment.model.entity.Payment;
import com.fpt.hivtreatment.model.entity.PaymentOrder;
import com.fpt.hivtreatment.model.entity.User;
import com.fpt.hivtreatment.repository.LabTestOrderRepository;
import com.fpt.hivtreatment.repository.PaymentOrderRepository;
import com.fpt.hivtreatment.repository.PaymentRepository;
import com.fpt.hivtreatment.repository.UserRepository;
import com.fpt.hivtreatment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;
    private final PaymentRepository paymentRepository;
    private final PaymentOrderRepository paymentOrderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LabTestOrderRepository labTestOrderRepository;

    /**
     * Create a new payment for lab test orders
     */
    @PostMapping
    public ResponseEntity<?> createPayment(@RequestBody CreatePaymentRequest request) {
        try {
            log.info("Creating payment for patient ID: {} with lab test orders: {}",
                    request.getPatientId(), request.getLabTestOrderIds());

            // Validate required data
            if (request.getPatientId() == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "Patient ID is required"));
            }

            if (request.getLabTestOrderIds() == null || request.getLabTestOrderIds().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("message", "At least one lab test order ID is required"));
            }

            // Find patient
            Optional<User> patientOpt = userRepository.findById(request.getPatientId());
            if (patientOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Patient not found"));
            }

            // Find lab test orders
            List<LabTestOrder> labTestOrders = labTestOrderRepository.findAllById(request.getLabTestOrderIds());
            if (labTestOrders.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "No valid lab test orders found"));
            }

            // Calculate total amount
            BigDecimal totalAmount = labTestOrders.stream()
                    .map(order -> order.getTestType() != null ? order.getTestType().getPrice() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Create payment entity
            Payment payment = Payment.builder()
                    .patient(patientOpt.get())
                    .status("Chờ thanh toán")
                    .totalAmount(totalAmount)
                    .createdAt(LocalDateTime.now())
                    .notes(request.getNotes())
                    .build();

            // Save payment
            payment = paymentRepository.save(payment);

            // Create payment orders
            Set<PaymentOrder> paymentOrders = new HashSet<>();
            for (LabTestOrder order : labTestOrders) {
                PaymentOrder paymentOrder = PaymentOrder.builder()
                        .payment(payment)
                        .labTestOrder(order)
                        .build();

                paymentOrders.add(paymentOrder);

                // Update lab test order status if needed
                if ("Đã tạo".equals(order.getStatus()) || "Chờ thanh toán".equals(order.getStatus())) {
                    order.setStatus("Chờ thanh toán");
                    labTestOrderRepository.save(order);
                }
            }

            // Save payment orders
            paymentOrderRepository.saveAll(paymentOrders);
            payment.setPaymentOrders(paymentOrders);

            // Return success response
            return ResponseEntity.ok(payment);

        } catch (Exception e) {
            log.error("Error creating payment", e);
            return ResponseEntity.badRequest().body(Map.of("message", "Error creating payment: " + e.getMessage()));
        }
    }

    /**
     * Get payment by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getPaymentById(@PathVariable Long id) {
        try {
            Payment payment = paymentService.getPaymentById(id);
            PaymentDTO paymentDTO = paymentService.convertToDTO(payment);
            return ResponseEntity.ok(paymentDTO);
        } catch (Exception e) {
            log.error("Error fetching payment with ID {}: {}", id, e.getMessage());
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Error fetching payment: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    /**
     * Get payments by patient ID
     */
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<?> getPaymentsByPatientId(@PathVariable Long patientId) {
        try {
            List<Payment> payments = paymentService.getPaymentsByPatientId(patientId);
            List<PaymentDTO> paymentDTOs = payments.stream()
                    .map(paymentService::convertToDTO)
                    .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("payments", paymentDTOs);
            response.put("totalItems", paymentDTOs.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching payments for patient {}: {}", patientId, e.getMessage());
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Error fetching payments: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Update payment status
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updatePaymentStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> statusRequest) {
        try {
            String status = statusRequest.get("status");
            if (status == null) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("message", "Status is required");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }

            log.info("Updating payment {} status to: {}", id, status);
            Payment payment = paymentService.updatePaymentStatus(id, status);
            PaymentDTO paymentDTO = paymentService.convertToDTO(payment);

            return ResponseEntity.ok(paymentDTO);
        } catch (Exception e) {
            log.error("Error updating payment {} status: {}", id, e.getMessage());
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Error updating payment status: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Update payment transaction information (from payment gateway callback)
     */
    @PutMapping("/{id}/transaction")
    public ResponseEntity<?> updatePaymentTransaction(
            @PathVariable Long id,
            @RequestBody Map<String, String> transactionData) {
        try {
            log.info("Updating payment {} transaction data: {}", id, transactionData);
            Payment payment = paymentService.updatePaymentTransaction(id, transactionData);
            PaymentDTO paymentDTO = paymentService.convertToDTO(payment);
            return ResponseEntity.ok(paymentDTO);
        } catch (Exception e) {
            log.error("Error updating payment {} transaction: {}", id, e.getMessage());
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Error updating payment transaction: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Get all payments in the system
     */
    @GetMapping
    public ResponseEntity<?> getAllPayments() {
        try {
            List<Payment> payments = paymentService.getAllPayments();
            List<PaymentDTO> paymentDTOs = payments.stream()
                    .map(paymentService::convertToDTO)
                    .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("payments", paymentDTOs);
            response.put("totalItems", paymentDTOs.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching all payments: {}", e.getMessage());
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Error fetching payments: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Update doctor notes for a payment
     */
    @PutMapping("/{id}/doctor-notes")
    public ResponseEntity<?> updateDoctorNotes(@PathVariable Long id, @RequestBody Map<String, String> request) {
        String doctorNote = request.get("doctorNote");

        Optional<Payment> paymentOpt = paymentRepository.findById(id);
        if (paymentOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Payment payment = paymentOpt.get();
        payment.setNotes(doctorNote);
        paymentRepository.save(payment);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Doctor notes updated successfully"));
    }

    /**
     * Process payment with multiple payment methods
     */
    @PostMapping("/{id}/process")
    public ResponseEntity<?> processPayment(@PathVariable Long id, @RequestBody ProcessPaymentRequest request) {
        try {
            log.info("Processing payment with ID: {} with {} payment methods",
                    id, request.getPaymentMethods() != null ? request.getPaymentMethods().size() : 0);

            // Set the payment ID from the path variable
            request.setPaymentId(id);

            // Process the payment
            Payment payment = paymentService.processPayment(request);

            // Convert to DTO
            PaymentDTO paymentDTO = paymentService.convertToDTO(payment);

            return ResponseEntity.ok(paymentDTO);
        } catch (Exception e) {
            log.error("Error processing payment with ID {}: {}", id, e.getMessage());
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Error processing payment: " + e.getMessage());

            if (e instanceof IllegalArgumentException) {
                return ResponseEntity.badRequest().body(errorResponse);
            }

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}