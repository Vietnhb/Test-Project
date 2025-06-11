package com.fpt.hivtreatment.controller;

import com.fpt.hivtreatment.dto.CreateTestOrderRequest;
import com.fpt.hivtreatment.model.entity.LabTestOrder;
import com.fpt.hivtreatment.model.entity.LabTestResult;
import com.fpt.hivtreatment.model.entity.MedicalRecordLabResult;
import com.fpt.hivtreatment.model.entity.TestType;
import com.fpt.hivtreatment.model.entity.User;
import com.fpt.hivtreatment.repository.LabTestOrderRepository;
import com.fpt.hivtreatment.repository.LabTestResultRepository;
import com.fpt.hivtreatment.repository.MedicalRecordLabResultRepository;
import com.fpt.hivtreatment.repository.MedicalRecordRepository;
import com.fpt.hivtreatment.repository.TestTypeRepository;
import com.fpt.hivtreatment.repository.UserRepository;
import com.fpt.hivtreatment.service.LabTestOrderService;
import com.fpt.hivtreatment.service.LabTestResultService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/lab-test-orders")
public class LabTestOrderController {

    @Autowired
    private LabTestOrderService labTestOrderService;

    @Autowired
    private TestTypeRepository testTypeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LabTestOrderRepository labTestOrderRepository;

    @Autowired
    private LabTestResultRepository labTestResultRepository;

    @Autowired
    private LabTestResultService labTestResultService;

    @Autowired
    private MedicalRecordLabResultRepository medicalRecordLabResultRepository;

    @Autowired
    private MedicalRecordRepository medicalRecordRepository;

    /**
     * Tạo đơn xét nghiệm mới
     */
    @PostMapping("/create")
    public ResponseEntity<?> createOrder(@RequestBody CreateTestOrderRequest request) {
        try {
            System.out.println("Received request for create: " + request);
            System.out.println("Request details: testTypeId=" + request.getTestTypeId() +
                    ", patientId=" + request.getPatientId() +
                    ", doctorId=" + request.getDoctorId() +
                    ", medicalRecordId=" + request.getMedicalRecordId());

            // Validate required parameters
            if (request.getTestTypeId() == null) {
                return ResponseEntity.badRequest().body("testTypeId is required");
            }

            if (request.getPatientId() == null) {
                return ResponseEntity.badRequest().body("patientId is required");
            }

            LabTestOrder order = labTestOrderService.createLabTestOrder(
                    request.getTestTypeId(),
                    request.getPatientId(),
                    request.getDoctorId(),
                    request.getMedicalRecordId(),
                    request.getNotes());

            Map<String, Object> response = new HashMap<>();
            response.put("orderId", order.getId());
            response.put("status", order.getStatus());
            response.put("orderDate", order.getOrderDate());
            response.put("testTypeName", order.getTestType().getName());
            response.put("price", order.getTestType().getPrice());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Lỗi khi tạo đơn xét nghiệm: " + e.getMessage());
        }
    }

    /**
     * Lấy danh sách đơn xét nghiệm của bệnh nhân
     */
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<?> getPatientOrders(@PathVariable Long patientId) {
        try {
            List<LabTestOrder> orders = labTestOrderService.getPatientOrders(patientId);

            // Chuyển đổi đơn hàng sang DTO để chuẩn hóa dữ liệu trả về và bổ sung thông tin
            // quan trọng
            List<Map<String, Object>> orderDTOs = orders.stream().map(order -> {
                Map<String, Object> orderDTO = new HashMap<>();
                orderDTO.put("id", order.getId());
                orderDTO.put("patientId", order.getPatient().getId());
                orderDTO.put("patientName", order.getPatient().getFullName());

                // Thông tin về bác sĩ (nếu có)
                if (order.getDoctor() != null) {
                    orderDTO.put("doctorId", order.getDoctor().getId());
                    orderDTO.put("doctorName", order.getDoctor().getFullName());
                }

                // Thông tin loại xét nghiệm
                TestType testType = order.getTestType();
                orderDTO.put("testTypeId", testType.getId());
                orderDTO.put("testType", new HashMap<String, Object>() {
                    {
                        put("id", testType.getId());
                        put("name", testType.getName());
                        put("description", testType.getDescription());
                        put("price", testType.getPrice());
                        put("category", testType.getCategory());
                        put("testGroup", testType.getTestGroup());
                    }
                });

                // Thông tin đơn hàng
                orderDTO.put("orderDate", order.getOrderDate());
                orderDTO.put("resultExpectedDate", order.getResultExpectedDate());
                orderDTO.put("status", order.getStatus());
                orderDTO.put("notes", order.getNotes());
                orderDTO.put("medicalRecordId", order.getMedicalRecordId());

                // Thêm thông tin thời gian tạo để frontend có thể sắp xếp
                orderDTO.put("createdAt", order.getCreatedAt());
                orderDTO.put("updatedAt", order.getUpdatedAt());

                return orderDTO;
            }).collect(Collectors.toList());

            if (orderDTOs.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("message", "Không có đơn xét nghiệm nào cho bệnh nhân này");
                response.put("orders", orderDTOs);
                return ResponseEntity.ok(response);
            }

            return ResponseEntity.ok(orderDTOs);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Lỗi khi lấy danh sách đơn xét nghiệm: " + e.getMessage());
        }
    }

    /**
     * Lấy danh sách đơn xét nghiệm HIV của bệnh nhân
     */
    @GetMapping("/patient/{patientId}/hiv")
    public ResponseEntity<?> getPatientHIVOrders(@PathVariable Long patientId) {
        try {
            List<LabTestOrder> orders = labTestOrderService.getPatientHIVOrders(patientId);

            // Chuyển đổi đơn hàng sang DTO để chuẩn hóa dữ liệu trả về và bổ sung thông tin
            // quan trọng
            List<Map<String, Object>> orderDTOs = orders.stream().map(order -> {
                Map<String, Object> orderDTO = new HashMap<>();
                orderDTO.put("id", order.getId());
                orderDTO.put("patientId", order.getPatient().getId());
                orderDTO.put("patientName", order.getPatient().getFullName());

                // Thông tin về bác sĩ (nếu có)
                if (order.getDoctor() != null) {
                    orderDTO.put("doctorId", order.getDoctor().getId());
                    orderDTO.put("doctorName", order.getDoctor().getFullName());
                }

                // Thông tin loại xét nghiệm
                TestType testType = order.getTestType();
                orderDTO.put("testTypeId", testType.getId());
                orderDTO.put("testType", new HashMap<String, Object>() {
                    {
                        put("id", testType.getId());
                        put("name", testType.getName());
                        put("description", testType.getDescription());
                        put("price", testType.getPrice());
                        put("category", testType.getCategory());
                        put("testGroup", testType.getTestGroup());
                    }
                });

                // Thông tin đơn hàng
                orderDTO.put("orderDate", order.getOrderDate());
                orderDTO.put("resultExpectedDate", order.getResultExpectedDate());
                orderDTO.put("status", order.getStatus());
                orderDTO.put("notes", order.getNotes());
                orderDTO.put("medicalRecordId", order.getMedicalRecordId());

                // Thêm thông tin thời gian tạo để frontend có thể sắp xếp
                orderDTO.put("createdAt", order.getCreatedAt());
                orderDTO.put("updatedAt", order.getUpdatedAt());

                return orderDTO;
            }).collect(Collectors.toList());

            if (orderDTOs.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("message", "Không có đơn xét nghiệm HIV nào cho bệnh nhân này");
                response.put("orders", orderDTOs);
                return ResponseEntity.ok(response);
            }

            return ResponseEntity.ok(orderDTOs);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Lỗi khi lấy danh sách đơn xét nghiệm HIV: " + e.getMessage());
        }
    }

    /**
     * Lấy danh sách đơn xét nghiệm theo hồ sơ y tế
     */
    @GetMapping("/medical-record/{medicalRecordId}")
    public ResponseEntity<?> getOrdersByMedicalRecord(@PathVariable Long medicalRecordId) {
        try {
            List<LabTestOrder> orders = labTestOrderService.getOrdersByMedicalRecord(medicalRecordId);

            // Chuyển đổi đơn hàng sang DTO để chuẩn hóa dữ liệu trả về và bổ sung thông tin
            // quan trọng
            List<Map<String, Object>> orderDTOs = orders.stream().map(order -> {
                Map<String, Object> orderDTO = new HashMap<>();
                orderDTO.put("id", order.getId());
                orderDTO.put("patientId", order.getPatient().getId());
                orderDTO.put("patientName", order.getPatient().getFullName());

                // Thông tin về bác sĩ (nếu có)
                if (order.getDoctor() != null) {
                    orderDTO.put("doctorId", order.getDoctor().getId());
                    orderDTO.put("doctorName", order.getDoctor().getFullName());
                }

                // Thông tin loại xét nghiệm
                TestType testType = order.getTestType();
                orderDTO.put("testTypeId", testType.getId());
                orderDTO.put("testType", new HashMap<String, Object>() {
                    {
                        put("id", testType.getId());
                        put("name", testType.getName());
                        put("description", testType.getDescription());
                        put("price", testType.getPrice());
                        put("category", testType.getCategory());
                        put("testGroup", testType.getTestGroup());
                    }
                });

                // Thông tin đơn hàng
                orderDTO.put("orderDate", order.getOrderDate());
                orderDTO.put("resultExpectedDate", order.getResultExpectedDate());
                orderDTO.put("status", order.getStatus());
                orderDTO.put("notes", order.getNotes());
                orderDTO.put("medicalRecordId", order.getMedicalRecordId());

                // Thêm thông tin thời gian tạo để frontend có thể sắp xếp
                orderDTO.put("createdAt", order.getCreatedAt());
                orderDTO.put("updatedAt", order.getUpdatedAt());

                return orderDTO;
            }).collect(Collectors.toList());

            if (orderDTOs.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("message", "Không có đơn xét nghiệm nào cho hồ sơ y tế này");
                response.put("orders", orderDTOs);
                return ResponseEntity.ok(response);
            }

            return ResponseEntity.ok(orderDTOs);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Lỗi khi lấy danh sách đơn xét nghiệm theo hồ sơ y tế: " + e.getMessage());
        }
    }

    /**
     * Lấy chi tiết đơn xét nghiệm
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<?> getOrderDetail(@PathVariable Long orderId) {
        try {
            Optional<LabTestOrder> orderOpt = labTestOrderService.getOrderById(orderId);
            if (orderOpt.isEmpty()) {
                Map<String, String> response = new HashMap<>();
                response.put("message", "Không tìm thấy đơn xét nghiệm với ID: " + orderId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            LabTestOrder order = orderOpt.get();
            Map<String, Object> orderDTO = new HashMap<>();
            orderDTO.put("id", order.getId());
            orderDTO.put("patientId", order.getPatient().getId());
            orderDTO.put("patientName", order.getPatient().getFullName());

            // Thông tin về bác sĩ (nếu có)
            if (order.getDoctor() != null) {
                orderDTO.put("doctorId", order.getDoctor().getId());
                orderDTO.put("doctorName", order.getDoctor().getFullName());
            }

            // Thông tin loại xét nghiệm
            TestType testType = order.getTestType();
            orderDTO.put("testTypeId", testType.getId());
            orderDTO.put("testType", new HashMap<String, Object>() {
                {
                    put("id", testType.getId());
                    put("name", testType.getName());
                    put("description", testType.getDescription());
                    put("price", testType.getPrice());
                    put("category", testType.getCategory());
                    put("testGroup", testType.getTestGroup());
                }
            });

            // Thông tin đơn hàng
            orderDTO.put("orderDate", order.getOrderDate());
            orderDTO.put("resultExpectedDate", order.getResultExpectedDate());
            orderDTO.put("status", order.getStatus());
            orderDTO.put("notes", order.getNotes());
            orderDTO.put("medicalRecordId", order.getMedicalRecordId());

            // Thêm thông tin thời gian tạo để frontend có thể sắp xếp
            orderDTO.put("createdAt", order.getCreatedAt());
            orderDTO.put("updatedAt", order.getUpdatedAt());

            return ResponseEntity.ok(orderDTO);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi lấy chi tiết đơn xét nghiệm: " + e.getMessage());
        }
    }

    /**
     * Cập nhật trạng thái đơn xét nghiệm
     */
    @PutMapping("/{orderId}/status")
    public ResponseEntity<?> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam String status) {
        try {
            LabTestOrder order = labTestOrderService.updateOrderStatus(orderId, status);

            Map<String, Object> response = new HashMap<>();
            response.put("orderId", order.getId());
            response.put("status", order.getStatus());
            response.put("updatedAt", order.getUpdatedAt());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Lỗi khi cập nhật trạng thái: " + e.getMessage());
        }
    }

    /**
     * Lấy danh sách loại xét nghiệm HIV
     */
    @GetMapping("/test-types/hiv")
    public ResponseEntity<?> getHIVTestTypes() {
        try {
            List<TestType> hivTests = testTypeRepository.findAllHIVTests();

            if (hivTests.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("message", "Không có loại xét nghiệm HIV nào");
                response.put("testTypes", hivTests);
                return ResponseEntity.ok(response);
            }

            return ResponseEntity.ok(hivTests);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi lấy danh sách xét nghiệm HIV: " + e.getMessage());
        }
    }

    /**
     * Lấy tất cả loại xét nghiệm
     */
    @GetMapping("/test-types")
    public ResponseEntity<?> getAllTestTypes() {
        try {
            List<TestType> testTypes = testTypeRepository.findAll();

            if (testTypes.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("message", "Không có loại xét nghiệm nào");
                response.put("testTypes", testTypes);
                return ResponseEntity.ok(response);
            }

            return ResponseEntity.ok(testTypes);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi lấy danh sách xét nghiệm: " + e.getMessage());
        }
    }

    /**
     * Lấy danh sách đơn hàng theo trạng thái (dành cho staff/admin)
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<?> getOrdersByStatus(@PathVariable String status) {
        try {
            List<LabTestOrder> orders = labTestOrderService.getOrdersByStatus(status);

            // Chuyển đổi đơn hàng sang DTO để chuẩn hóa dữ liệu trả về và bổ sung thông tin
            // quan trọng
            List<Map<String, Object>> orderDTOs = orders.stream().map(order -> {
                Map<String, Object> orderDTO = new HashMap<>();
                orderDTO.put("id", order.getId());
                orderDTO.put("patientId", order.getPatient().getId());
                orderDTO.put("patientName", order.getPatient().getFullName());

                // Thông tin về bác sĩ (nếu có)
                if (order.getDoctor() != null) {
                    orderDTO.put("doctorId", order.getDoctor().getId());
                    orderDTO.put("doctorName", order.getDoctor().getFullName());
                }

                // Thông tin loại xét nghiệm
                TestType testType = order.getTestType();
                orderDTO.put("testTypeId", testType.getId());
                orderDTO.put("testType", new HashMap<String, Object>() {
                    {
                        put("id", testType.getId());
                        put("name", testType.getName());
                        put("description", testType.getDescription());
                        put("price", testType.getPrice());
                        put("category", testType.getCategory());
                        put("testGroup", testType.getTestGroup());
                    }
                });

                // Thông tin đơn hàng
                orderDTO.put("orderDate", order.getOrderDate());
                orderDTO.put("resultExpectedDate", order.getResultExpectedDate());
                orderDTO.put("status", order.getStatus());
                orderDTO.put("notes", order.getNotes());
                orderDTO.put("medicalRecordId", order.getMedicalRecordId());

                // Thêm thông tin thời gian tạo để frontend có thể sắp xếp
                orderDTO.put("createdAt", order.getCreatedAt());
                orderDTO.put("updatedAt", order.getUpdatedAt());

                return orderDTO;
            }).collect(Collectors.toList());

            if (orderDTOs.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("message", "Không có đơn xét nghiệm nào ở trạng thái: " + status);
                response.put("orders", orderDTOs);
                return ResponseEntity.ok(response);
            }

            return ResponseEntity.ok(orderDTOs);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi lấy danh sách đơn hàng: " + e.getMessage());
        }
    }

    /**
     * Lấy thống kê đơn hàng
     */
    @GetMapping("/statistics")
    public ResponseEntity<?> getOrderStatistics() {
        try {
            Map<String, Object> stats = new HashMap<>();
            stats.put("pendingPayment", labTestOrderService.countOrdersByStatus("Chờ thanh toán"));
            stats.put("pendingSample", labTestOrderService.countOrdersByStatus("Chờ lấy mẫu"));
            stats.put("processing", labTestOrderService.countOrdersByStatus("Đang xử lý"));
            stats.put("completed", labTestOrderService.countOrdersByStatus("Có kết quả"));

            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi lấy thống kê: " + e.getMessage());
        }
    }

    /**
     * Test endpoint - simple health check
     */
    @GetMapping("/test")
    public ResponseEntity<?> testEndpoint() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Lab test orders API is working");
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }

    /**
     * Lấy nhiều đơn xét nghiệm cùng lúc theo danh sách ID
     */
    @PostMapping("/batch")
    public ResponseEntity<?> getOrdersByIds(@RequestBody Map<String, List<Long>> request) {
        try {
            List<Long> orderIds = request.get("orderIds");

            if (orderIds == null || orderIds.isEmpty()) {
                Map<String, String> response = new HashMap<>();
                response.put("message", "Danh sách ID đơn xét nghiệm không được để trống");
                return ResponseEntity.badRequest().body(response);
            }

            System.out.println("Fetching orders by IDs: " + orderIds);

            List<Map<String, Object>> result = new ArrayList<>();

            for (Long orderId : orderIds) {
                Optional<LabTestOrder> orderOpt = labTestOrderService.getOrderById(orderId);

                if (orderOpt.isPresent()) {
                    LabTestOrder order = orderOpt.get();
                    Map<String, Object> orderDTO = new HashMap<>();
                    orderDTO.put("id", order.getId());
                    orderDTO.put("patientId", order.getPatient().getId());
                    orderDTO.put("patientName", order.getPatient().getFullName());

                    // Thông tin về bác sĩ (nếu có)
                    if (order.getDoctor() != null) {
                        orderDTO.put("doctorId", order.getDoctor().getId());
                        orderDTO.put("doctorName", order.getDoctor().getFullName());
                    }

                    // Thông tin loại xét nghiệm
                    TestType testType = order.getTestType();
                    orderDTO.put("testTypeId", testType.getId());
                    orderDTO.put("testType", new HashMap<String, Object>() {
                        {
                            put("id", testType.getId());
                            put("name", testType.getName());
                            put("description", testType.getDescription());
                            put("price", testType.getPrice());
                            put("category", testType.getCategory());
                            put("testGroup", testType.getTestGroup());
                            put("requiresFasting", testType.getRequiresFasting());
                            put("defaultTurnaroundTimeHours", testType.getDefaultTurnaroundTimeHours());
                        }
                    });

                    // Thông tin đơn hàng
                    orderDTO.put("orderDate", order.getOrderDate());
                    orderDTO.put("resultExpectedDate", order.getResultExpectedDate());
                    orderDTO.put("status", order.getStatus());
                    orderDTO.put("notes", order.getNotes());
                    orderDTO.put("medicalRecordId", order.getMedicalRecordId());

                    // Thêm thông tin thời gian tạo để frontend có thể sắp xếp
                    orderDTO.put("createdAt", order.getCreatedAt());
                    orderDTO.put("updatedAt", order.getUpdatedAt());

                    result.add(orderDTO);
                }
            }

            if (result.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("message", "Không tìm thấy đơn xét nghiệm nào với các ID đã cung cấp");
                response.put("orders", result);
                return ResponseEntity.ok(response);
            }

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> response = new HashMap<>();
            response.put("message", "Lỗi khi lấy đơn xét nghiệm theo ID: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Cập nhật ngày dự kiến trả kết quả xét nghiệm
     */
    @PutMapping("/{orderId}/expected-date")
    public ResponseEntity<?> updateExpectedDate(
            @PathVariable Long orderId,
            @RequestBody Map<String, Object> request) {
        try {
            if (!request.containsKey("resultExpectedDate")) {
                return ResponseEntity.badRequest().body("Ngày dự kiến trả kết quả là bắt buộc");
            }

            String resultExpectedDate = (String) request.get("resultExpectedDate");
            String notes = request.containsKey("notes") ? (String) request.get("notes") : "";

            LabTestOrder order = labTestOrderService.updateExpectedResultDate(orderId, resultExpectedDate, notes);

            Map<String, Object> response = new HashMap<>();
            response.put("orderId", order.getId());
            response.put("resultExpectedDate", order.getResultExpectedDate());
            response.put("status", order.getStatus());
            response.put("updatedAt", order.getUpdatedAt());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Lỗi khi cập nhật ngày dự kiến trả kết quả: " + e.getMessage());
        }
    }

    /**
     * Create or update lab test result for a specific order
     */
    @PostMapping("/{id}/results")
    public ResponseEntity<?> createOrUpdateLabTestResult(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        try {
            // Extract data from request
            String value = request.get("value") != null ? request.get("value").toString() : "";
            String unit = request.get("unit") != null ? request.get("unit").toString() : "";
            String referenceRange = request.get("referenceRange") != null ? request.get("referenceRange").toString()
                    : "";
            String notes = request.get("notes") != null ? request.get("notes").toString() : "";
            String conclusion = request.get("conclusion") != null ? request.get("conclusion").toString() : "";
            String status = request.get("status") != null ? request.get("status").toString() : "Có kết quả";
            String attachmentsPath = request.get("attachmentsPath") != null ? request.get("attachmentsPath").toString()
                    : null;
            Long medicalRecordId = null;
            Long enteredByUserId = null;

            // Extract medicalRecordId if available
            if (request.get("medicalRecordId") != null) {
                try {
                    medicalRecordId = Long.parseLong(request.get("medicalRecordId").toString());
                } catch (NumberFormatException e) {
                    // Ignore parsing error, keep medicalRecordId as null
                }
            } // Extract entered_by_user_id if available
            if (request.get("entered_by_user_id") != null) {
                try {
                    enteredByUserId = Long.parseLong(request.get("entered_by_user_id").toString());
                    System.out.println("Received entered_by_user_id: " + enteredByUserId);
                } catch (NumberFormatException e) {
                    System.err.println("Error parsing entered_by_user_id: " + e.getMessage());
                }
            }

            // VALIDATION: Bắt buộc phải có entered_by_user_id
            if (enteredByUserId == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Lỗi: Không xác định được người nhập kết quả. Vui lòng đăng nhập lại và thử lại.");
            }

            // Find the lab test order
            Optional<LabTestOrder> orderOpt = labTestOrderService.getOrderById(id);
            if (orderOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Không tìm thấy đơn xét nghiệm với ID: " + id);
            }

            LabTestOrder order = orderOpt.get();

            // Check if order status is "Chờ lấy mẫu" - prevent creating results
            if (order.getStatus().equals("Chờ lấy mẫu")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Không thể tạo kết quả cho xét nghiệm đang ở trạng thái Chờ lấy mẫu");
            }

            // 1. Create or update the lab test result
            // Create JSON structure for the result data
            Map<String, String> resultDataMap = new HashMap<>();
            resultDataMap.put("value", value);
            resultDataMap.put("unit", unit);
            resultDataMap.put("referenceRange", referenceRange);
            resultDataMap.put("conclusion", conclusion);

            String resultDataJson;
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                resultDataJson = objectMapper.writeValueAsString(resultDataMap);
            } catch (Exception e) {
                throw new RuntimeException("Error converting result data to JSON: " + e.getMessage());
            }

            // Get user for test result entry - priority to request's entered_by_user_id
            User staffUser = null;

            // Nếu có enteredByUserId từ request, sử dụng nó
            if (enteredByUserId != null) {
                Optional<User> userOpt = userRepository.findById(enteredByUserId);
                if (userOpt.isPresent()) {
                    staffUser = userOpt.get();
                    System.out.println(
                            "Using user from request ID: " + enteredByUserId + ", username: " + staffUser.getEmail());
                } else {
                    System.err.println(
                            "User with ID " + enteredByUserId + " not found, falling back to authenticated user");
                }
            }

            // Fallback: lấy user từ token JWT nếu không có từ request hoặc không tìm thấy
            // user
            if (staffUser == null) {
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                String username = auth.getName();
                Optional<User> staffUserOpt = userRepository.findByEmail(username);
                staffUser = staffUserOpt.orElse(null);
                System.out.println("Using authenticated user: " + (staffUser != null ? staffUser.getEmail() : "none"));
            } // Create or update the lab test result using the service method
            Long userIdToPass = staffUser != null ? staffUser.getId() : null;
            System.out.println("Passing enteredByUserId to service: " + userIdToPass);

            LabTestResult savedResult = labTestResultService.createOrUpdateLabTestResult(
                    id, // testOrderId
                    resultDataJson,
                    conclusion,
                    notes,
                    attachmentsPath,
                    userIdToPass // enteredByUserId
            );
            System.out.println("Saved result enteredByUser: " +
                    (savedResult.getEnteredByUser() != null ? savedResult.getEnteredByUser().getId() : "null"));

            // Force flush to ensure data is saved immediately
            if (savedResult.getEnteredByUser() == null && userIdToPass != null) {
                System.err.println("ERROR: enteredByUser was not saved! Attempting manual save...");
                // Manual fallback - direct repository save
                savedResult.setEnteredByUser(staffUser);
                labTestResultRepository.save(savedResult);
                System.out.println("Manual save completed. enteredByUser now: " +
                        (savedResult.getEnteredByUser() != null ? savedResult.getEnteredByUser().getId() : "null"));
            }

            // Update order status
            labTestResultService.updateOrderStatus(id, status);

            // 3. If medicalRecordId is provided, create association between result and
            // medical record
            if (medicalRecordId != null) {
                try {
                    // Check if medical record exists
                    if (!medicalRecordRepository.existsById(medicalRecordId)) {
                        throw new RuntimeException("Medical record with ID " + medicalRecordId + " not found");
                    }

                    // Check if association already exists
                    boolean exists = medicalRecordLabResultRepository.existsByMedicalRecordIdAndLabTestResultId(
                            medicalRecordId, savedResult.getId());

                    if (!exists) {
                        // Create new association
                        MedicalRecordLabResult association = new MedicalRecordLabResult();
                        association.setMedicalRecord(medicalRecordRepository.findById(medicalRecordId).get());
                        association.setLabTestResult(savedResult);
                        association.setResultInterpretation(conclusion); // Initial interpretation based on conclusion
                        association.setCreatedAt(LocalDateTime.now());

                        medicalRecordLabResultRepository.save(association);
                    }
                } catch (Exception e) {
                    // Log error but don't fail the whole request
                    System.err.println("Error associating result with medical record: " + e.getMessage());
                }
            }

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Lưu kết quả xét nghiệm thành công");
            response.put("resultId", savedResult.getId());
            response.put("orderId", order.getId());
            response.put("status", order.getStatus());

            if (medicalRecordId != null) {
                response.put("medicalRecordId", medicalRecordId);
                response.put("linkedToMedicalRecord", true);
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi lưu kết quả xét nghiệm: " + e.getMessage());
        }
    }

    /**
     * Record lab test result to patient (without sending email)
     */
    @PostMapping("/{id}/send-to-patient")
    public ResponseEntity<?> recordLabTestResultToPatient(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        try {
            // Extract data from request
            String message = request.get("message") != null ? request.get("message").toString() : "";
            boolean includeAttachments = request.get("includeAttachments") != null &&
                    Boolean.parseBoolean(request.get("includeAttachments").toString());

            // Find the lab test order
            Optional<LabTestOrder> orderOpt = labTestOrderService.getOrderById(id);
            if (orderOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Không tìm thấy đơn xét nghiệm với ID: " + id);
            }

            // Find the result
            LabTestResult result = labTestResultService.findByLabTestOrderId(id).orElse(null);
            if (result == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Không tìm thấy kết quả xét nghiệm cho đơn hàng có ID: " + id);
            }

            // Ở đây chỉ ghi nhận yêu cầu, không thực sự gửi email
            // TODO: Bổ sung thêm logic để đánh dấu kết quả đã được gửi cho bệnh nhân nếu
            // cần

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Đã ghi nhận yêu cầu gửi kết quả xét nghiệm đến bệnh nhân");
            response.put("note", "Chức năng gửi email chưa được triển khai");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi ghi nhận yêu cầu gửi kết quả xét nghiệm: " + e.getMessage());
        }
    }
}
