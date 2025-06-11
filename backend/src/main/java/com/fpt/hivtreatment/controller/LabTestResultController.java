package com.fpt.hivtreatment.controller;

import com.fpt.hivtreatment.model.entity.LabTestResult;
import com.fpt.hivtreatment.service.LabTestResultService;
import com.fpt.hivtreatment.service.MedicalRecordLabResultService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fpt.hivtreatment.dto.LabTestResultDTO;
import com.fpt.hivtreatment.service.impl.LabTestResultServiceImpl;
import com.fpt.hivtreatment.model.entity.LabTestOrder;
import com.fpt.hivtreatment.repository.LabTestOrderRepository;

@RestController
@RequestMapping("/api/lab-test-results")
public class LabTestResultController {

    @Autowired
    private LabTestResultService labTestResultService;

    @Autowired
    private LabTestResultServiceImpl labTestResultServiceImpl;

    @Autowired
    private MedicalRecordLabResultService medicalRecordLabResultService;

    @Autowired
    private LabTestOrderRepository labTestOrderRepository;

    /**
     * Get all lab test results
     */
    @GetMapping
    public ResponseEntity<?> getAllLabTestResults() {
        try {
            List<LabTestResult> results = labTestResultService.findAll();

            if (results.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("message", "Không có kết quả xét nghiệm nào");
                response.put("results", results);
                return ResponseEntity.ok(response);
            }

            return ResponseEntity.ok(results);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi lấy danh sách kết quả xét nghiệm: " + e.getMessage());
        }
    }

    /**
     * Get a lab test result by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getLabTestResultById(@PathVariable Long id) {
        try {
            Optional<LabTestResult> resultOpt = labTestResultService.findById(id);

            if (resultOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Không tìm thấy kết quả xét nghiệm với ID: " + id);
            }

            return ResponseEntity.ok(resultOpt.get());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi lấy kết quả xét nghiệm: " + e.getMessage());
        }
    }

    /**
     * Get a lab test result by lab test order ID
     */
    @GetMapping("/order/{orderId}")
    public ResponseEntity<?> getLabTestResultByOrderId(@PathVariable Long orderId) {
        try {
            Optional<LabTestResult> resultOpt = labTestResultService.findByLabTestOrderId(orderId);

            if (resultOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Không tìm thấy kết quả xét nghiệm cho đơn hàng có ID: " + orderId);
            }

            return ResponseEntity.ok(resultOpt.get());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi lấy kết quả xét nghiệm: " + e.getMessage());
        }
    }

    /**
     * Get a lab test result by lab test order ID - alternative path for frontend
     * compatibility
     */
    @GetMapping("/by-order/{orderId}")
    public ResponseEntity<?> getLabTestResultByLabTestOrderId(@PathVariable Long orderId) {
        try {
            Optional<LabTestResult> resultOpt = labTestResultService.findByLabTestOrderId(orderId);

            if (resultOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Không tìm thấy kết quả xét nghiệm cho đơn hàng có ID: " + orderId);
            }

            // Convert to DTO with additional details
            LabTestResultDTO resultDTO = labTestResultServiceImpl.convertToDTO(resultOpt.get());
            return ResponseEntity.ok(resultDTO);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi lấy kết quả xét nghiệm: " + e.getMessage());
        }
    }

    /**
     * Get all lab test results for a patient
     */
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<?> getLabTestResultsByPatientId(@PathVariable Long patientId) {
        try {
            List<LabTestResult> results = labTestResultService.findByPatientId(patientId);

            if (results.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("message", "Không có kết quả xét nghiệm nào cho bệnh nhân có ID: " + patientId);
                response.put("results", results);
                return ResponseEntity.ok(response);
            }

            return ResponseEntity.ok(results);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi lấy danh sách kết quả xét nghiệm: " + e.getMessage());
        }
    }

    /**
     * Get all lab test results for a doctor
     */
    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<?> getLabTestResultsByDoctorId(@PathVariable Long doctorId) {
        try {
            List<LabTestResult> results = labTestResultService.findByDoctorId(doctorId);

            if (results.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("message", "Không có kết quả xét nghiệm nào cho bác sĩ có ID: " + doctorId);
                response.put("results", results);
                return ResponseEntity.ok(response);
            }

            return ResponseEntity.ok(results);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi lấy danh sách kết quả xét nghiệm: " + e.getMessage());
        }
    }

    /**
     * Get all lab test results for a test type
     */
    @GetMapping("/test-type/{testTypeId}")
    public ResponseEntity<?> getLabTestResultsByTestTypeId(@PathVariable Long testTypeId) {
        try {
            List<LabTestResult> results = labTestResultService.findByTestTypeId(testTypeId);

            if (results.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("message", "Không có kết quả xét nghiệm nào cho loại xét nghiệm có ID: " + testTypeId);
                response.put("results", results);
                return ResponseEntity.ok(response);
            }

            return ResponseEntity.ok(results);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi lấy danh sách kết quả xét nghiệm: " + e.getMessage());
        }
    }

    /**
     * Get all lab test results for a patient and test type
     */
    @GetMapping("/patient/{patientId}/test-type/{testTypeId}")
    public ResponseEntity<?> getLabTestResultsByPatientIdAndTestTypeId(
            @PathVariable Long patientId,
            @PathVariable Long testTypeId) {
        try {
            List<LabTestResult> results = labTestResultService.findByPatientIdAndTestTypeId(patientId, testTypeId);

            if (results.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("message", "Không có kết quả xét nghiệm nào cho bệnh nhân có ID: " + patientId +
                        " và loại xét nghiệm có ID: " + testTypeId);
                response.put("results", results);
                return ResponseEntity.ok(response);
            }

            return ResponseEntity.ok(results);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi lấy danh sách kết quả xét nghiệm: " + e.getMessage());
        }
    }

    /**
     * Get the latest lab test result for a patient and test type
     */
    @GetMapping("/patient/{patientId}/test-type/{testTypeId}/latest")
    public ResponseEntity<?> getLatestLabTestResultByPatientIdAndTestTypeId(
            @PathVariable Long patientId,
            @PathVariable Long testTypeId) {
        try {
            Optional<LabTestResult> resultOpt = labTestResultService.findLatestByPatientIdAndTestTypeId(patientId,
                    testTypeId);

            if (resultOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Không tìm thấy kết quả xét nghiệm nào cho bệnh nhân có ID: " + patientId +
                                " và loại xét nghiệm có ID: " + testTypeId);
            }

            return ResponseEntity.ok(resultOpt.get());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi lấy kết quả xét nghiệm: " + e.getMessage());
        }
    }

    /**
     * Update attachments path for a lab test result
     */
    @PutMapping("/{id}/attachments")
    public ResponseEntity<?> updateAttachmentsPath(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        try {
            if (!request.containsKey("attachmentsPath")) {
                return ResponseEntity.badRequest().body("Thiếu thông tin đường dẫn tệp đính kèm");
            }

            String attachmentsPath = request.get("attachmentsPath");
            LabTestResult updatedResult = labTestResultService.updateAttachmentsPath(id, attachmentsPath);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Cập nhật đường dẫn tệp đính kèm thành công");
            response.put("result", updatedResult);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi cập nhật đường dẫn tệp đính kèm: " + e.getMessage());
        }
    }

    /**
     * Associate a lab test result with a medical record
     */
    @PostMapping("/{id}/medical-records/{medicalRecordId}")
    public ResponseEntity<?> associateWithMedicalRecord(
            @PathVariable Long id,
            @PathVariable Long medicalRecordId,
            @RequestBody Map<String, String> request) {
        try {
            String resultInterpretation = request.getOrDefault("resultInterpretation", "");
            String clinicalSignificance = request.getOrDefault("clinicalSignificance", "");

            labTestResultService.associateWithMedicalRecord(id, medicalRecordId, resultInterpretation,
                    clinicalSignificance);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Liên kết kết quả xét nghiệm với hồ sơ bệnh án thành công");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi liên kết kết quả xét nghiệm với hồ sơ bệnh án: " + e.getMessage());
        }
    }

    /**
     * Delete a lab test result
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteLabTestResult(@PathVariable Long id) {
        try {
            // Check if result exists
            if (labTestResultService.findById(id).isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Không tìm thấy kết quả xét nghiệm với ID: " + id);
            }

            labTestResultService.deleteLabTestResult(id);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Xóa kết quả xét nghiệm thành công");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi xóa kết quả xét nghiệm: " + e.getMessage());
        }
    }

    /**
     * Test endpoint
     */
    @GetMapping("/test")
    public ResponseEntity<?> testEndpoint() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "API kết quả xét nghiệm đang hoạt động");
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }

    /**
     * Create or update a lab test result for a specific test order
     */
    @PostMapping("/orders/{testOrderId}")
    public ResponseEntity<?> createOrUpdateLabTestResult(
            @PathVariable Long testOrderId,
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

            // Check if order status is "Chờ lấy mẫu" - prevent creating results
            Optional<LabTestOrder> orderOpt = labTestOrderRepository.findById(testOrderId);
            if (orderOpt.isPresent() && orderOpt.get().getStatus().equals("Chờ lấy mẫu")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Không thể tạo kết quả cho xét nghiệm đang ở trạng thái Chờ lấy mẫu");
            }

            // Create JSON structure for the result data
            Map<String, String> resultDataMap = new HashMap<>();
            resultDataMap.put("value", value);
            resultDataMap.put("unit", unit);
            resultDataMap.put("referenceRange", referenceRange);
            resultDataMap.put("conclusion", conclusion);
            ObjectMapper objectMapper = new ObjectMapper();
            String resultDataJson = objectMapper.writeValueAsString(resultDataMap);

            // Extract entered_by_user_id if available
            Long enteredByUserId = null;
            if (request.get("entered_by_user_id") != null) {
                try {
                    enteredByUserId = Long.parseLong(request.get("entered_by_user_id").toString());
                } catch (NumberFormatException e) {
                    // Ignore parsing error, keep enteredByUserId as null
                }
            }

            // Create or update the lab test result
            LabTestResult result = labTestResultService.createOrUpdateLabTestResult(
                    testOrderId,
                    resultDataJson,
                    conclusion,
                    notes,
                    attachmentsPath,
                    enteredByUserId);

            // Update the order status
            labTestResultService.updateOrderStatus(testOrderId, status);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Lưu kết quả xét nghiệm thành công");
            response.put("resultId", result.getId());
            response.put("orderId", testOrderId);
            response.put("status", status);

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

            // Get the lab test result
            Optional<LabTestResult> resultOpt = labTestResultService.findById(id);
            if (resultOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Không tìm thấy kết quả xét nghiệm với ID: " + id);
            }

            // Add logic to mark the result as "sent to patient" in the database if needed
            // For now, we're just recording the request without actually sending it

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Đã ghi nhận yêu cầu gửi kết quả xét nghiệm đến bệnh nhân");
            response.put("note", "Chức năng gửi email chưa được triển khai");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi ghi nhận yêu cầu gửi kết quả xét nghiệm: " + e.getMessage());
        }
    }

    /**
     * Get all completed test results
     */
    @GetMapping("/completed")
    public ResponseEntity<?> getCompletedTestResults() {
        try {
            List<LabTestResult> results = labTestResultService.findCompleteResultsWithDetails();

            if (results.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("message", "Không có kết quả xét nghiệm hoàn thành nào");
                response.put("results", results);
                return ResponseEntity.ok(response);
            }

            // Convert to DTOs with additional details
            List<LabTestResultDTO> resultDTOs = labTestResultServiceImpl.convertToDTOList(results);
            return ResponseEntity.ok(resultDTOs);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi lấy danh sách kết quả xét nghiệm hoàn thành: " + e.getMessage());
        }
    }
}
// ihiohoi