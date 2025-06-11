package com.fpt.hivtreatment.service.impl;

import com.fpt.hivtreatment.model.entity.LabTestOrder;
import com.fpt.hivtreatment.model.entity.LabTestResult;
import com.fpt.hivtreatment.model.entity.MedicalRecord;
import com.fpt.hivtreatment.model.entity.MedicalRecordLabResult;
import com.fpt.hivtreatment.model.entity.User;
import com.fpt.hivtreatment.repository.LabTestOrderRepository;
import com.fpt.hivtreatment.repository.LabTestResultRepository;
import com.fpt.hivtreatment.repository.MedicalRecordLabResultRepository;
import com.fpt.hivtreatment.repository.MedicalRecordRepository;
import com.fpt.hivtreatment.repository.UserRepository;
import com.fpt.hivtreatment.service.LabTestResultService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.fpt.hivtreatment.dto.LabTestResultDTO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
@Transactional
public class LabTestResultServiceImpl implements LabTestResultService {

    @Autowired
    private LabTestResultRepository labTestResultRepository;

    @Autowired
    private LabTestOrderRepository labTestOrderRepository;

    @Autowired
    private MedicalRecordRepository medicalRecordRepository;
    @Autowired
    private MedicalRecordLabResultRepository medicalRecordLabResultRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public LabTestResult saveLabTestResult(LabTestResult labTestResult) {
        // Set creation date if not set
        if (labTestResult.getCreatedAt() == null) {
            labTestResult.setCreatedAt(LocalDateTime.now());
        }
        labTestResult.setUpdatedAt(LocalDateTime.now());
        return labTestResultRepository.save(labTestResult);
    }

    @Override
    public LabTestResult updateLabTestResult(Long id, LabTestResult labTestResultDetails) {
        Optional<LabTestResult> labTestResultOpt = labTestResultRepository.findById(id);
        if (labTestResultOpt.isEmpty()) {
            throw new RuntimeException("Lab test result not found with id: " + id);
        }

        LabTestResult existingResult = labTestResultOpt.get();

        // Update fields
        existingResult.setResultData(labTestResultDetails.getResultData());
        existingResult.setResultSummary(labTestResultDetails.getResultSummary());

        if (labTestResultDetails.getAttachmentsPath() != null) {
            existingResult.setAttachmentsPath(labTestResultDetails.getAttachmentsPath());
        }

        existingResult.setNotes(labTestResultDetails.getNotes());
        existingResult.setUpdatedAt(LocalDateTime.now());

        return labTestResultRepository.save(existingResult);
    }

    @Override
    public Optional<LabTestResult> findById(Long id) {
        return labTestResultRepository.findById(id);
    }

    @Override
    public Optional<LabTestResult> findByLabTestOrderId(Long labTestOrderId) {
        return labTestResultRepository.findByLabTestOrderId(labTestOrderId);
    }

    @Override
    public List<LabTestResult> findByPatientId(Long patientId) {
        return labTestResultRepository.findByPatientId(patientId);
    }

    @Override
    public List<LabTestResult> findByDoctorId(Long doctorId) {
        return labTestResultRepository.findByDoctorId(doctorId);
    }

    @Override
    public List<LabTestResult> findByTestTypeId(Long testTypeId) {
        return labTestResultRepository.findByTestTypeId(testTypeId);
    }

    @Override
    public List<LabTestResult> findByPatientIdAndTestTypeId(Long patientId, Long testTypeId) {
        return labTestResultRepository.findByPatientIdAndTestTypeId(patientId, testTypeId);
    }

    @Override
    public Optional<LabTestResult> findLatestByPatientIdAndTestTypeId(Long patientId, Long testTypeId) {
        List<LabTestResult> results = labTestResultRepository.findLatestByPatientIdAndTestTypeId(patientId, testTypeId);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    @Override
    public LabTestResult updateAttachmentsPath(Long id, String attachmentsPath) {
        Optional<LabTestResult> labTestResultOpt = labTestResultRepository.findById(id);
        if (labTestResultOpt.isEmpty()) {
            throw new RuntimeException("Lab test result not found with id: " + id);
        }

        LabTestResult labTestResult = labTestResultOpt.get();
        labTestResult.setAttachmentsPath(attachmentsPath);
        labTestResult.setUpdatedAt(LocalDateTime.now());

        return labTestResultRepository.save(labTestResult);
    }

    @Override
    public void deleteLabTestResult(Long id) {
        // First, delete any associations with medical records
        medicalRecordLabResultRepository.findByLabTestResultId(id)
                .forEach(mrlr -> medicalRecordLabResultRepository.delete(mrlr));

        // Then delete the lab test result
        labTestResultRepository.deleteById(id);
    }

    @Override
    public void associateWithMedicalRecord(Long labTestResultId, Long medicalRecordId, String resultInterpretation,
            String clinicalSignificance) {
        // Find the lab test result
        Optional<LabTestResult> labTestResultOpt = labTestResultRepository.findById(labTestResultId);
        if (labTestResultOpt.isEmpty()) {
            throw new RuntimeException("Lab test result not found with id: " + labTestResultId);
        }

        // Find the medical record
        Optional<MedicalRecord> medicalRecordOpt = medicalRecordRepository.findById(medicalRecordId);
        if (medicalRecordOpt.isEmpty()) {
            throw new RuntimeException("Medical record not found with id: " + medicalRecordId);
        }

        // Check if association already exists
        boolean exists = medicalRecordLabResultRepository.existsByMedicalRecordIdAndLabTestResultId(medicalRecordId,
                labTestResultId);
        if (exists) {
            throw new RuntimeException("Association already exists between this medical record and lab test result");
        }

        // Create new association
        MedicalRecordLabResult association = new MedicalRecordLabResult();
        association.setMedicalRecord(medicalRecordOpt.get());
        association.setLabTestResult(labTestResultOpt.get());
        association.setResultInterpretation(resultInterpretation);

        medicalRecordLabResultRepository.save(association);
    }

    @Override
    public List<LabTestResult> findAll() {
        return labTestResultRepository.findAll();
    }

    @Override
    public LabTestResult createOrUpdateLabTestResult(
            Long testOrderId,
            String value,
            String unit,
            String referenceRange,
            String notes,
            String conclusion,
            String status,
            String attachmentsPath,
            Long enteredByUserId) {

        // Get the lab test order
        LabTestOrder order = labTestOrderRepository.findById(testOrderId)
                .orElseThrow(() -> new RuntimeException("Lab test order not found with ID: " + testOrderId));

        // Check if order status is "Chờ lấy mẫu" - prevent creating results
        if (order.getStatus().equals("Chờ lấy mẫu")) {
            throw new RuntimeException("Không thể tạo kết quả cho xét nghiệm đang ở trạng thái Chờ lấy mẫu");
        }

        // Get the user who entered the result if provided
        User enteredByUser = null;
        if (enteredByUserId != null) {
            enteredByUser = userRepository.findById(enteredByUserId)
                    .orElseThrow(() -> new RuntimeException("User not found with ID: " + enteredByUserId));
        }

        // Find existing result or create new one
        Optional<LabTestResult> existingResult = findByLabTestOrderId(testOrderId);
        LabTestResult result;

        if (existingResult.isPresent()) {
            // Update existing result
            result = existingResult.get();
        } else {
            // Create new result
            result = new LabTestResult();
            result.setLabTestOrder(order);
            result.setCreatedAt(LocalDateTime.now());
            result.setResultDate(LocalDateTime.now());
        }

        // Create JSON structure for the result data
        String resultDataJson = String.format(
                "{\"value\":\"%s\",\"unit\":\"%s\",\"referenceRange\":\"%s\",\"conclusion\":\"%s\"}",
                value, unit, referenceRange, conclusion);

        // Set/update values
        result.setResultData(resultDataJson);
        result.setResultSummary(conclusion);
        result.setNotes(notes);
        result.setAttachmentsPath(attachmentsPath);
        result.setEnteredByUser(enteredByUser); // Set the user who entered the result
        result.setUpdatedAt(LocalDateTime.now());

        // Save and return
        return labTestResultRepository.save(result);
    }

    @Override
    public LabTestResult createOrUpdateLabTestResult(
            Long testOrderId,
            String resultDataJson,
            String conclusion,
            String notes,
            String attachmentsPath,
            Long enteredByUserId) {

        // Get the lab test order
        LabTestOrder order = labTestOrderRepository.findById(testOrderId)
                .orElseThrow(() -> new RuntimeException("Lab test order not found with ID: " + testOrderId));

        // Check if order status is "Chờ lấy mẫu" - prevent creating results
        if (order.getStatus().equals("Chờ lấy mẫu")) {
            throw new RuntimeException("Không thể tạo kết quả cho xét nghiệm đang ở trạng thái Chờ lấy mẫu");
        }

        // Get the user who entered the result if provided
        User enteredByUser = null;
        if (enteredByUserId != null) {
            enteredByUser = userRepository.findById(enteredByUserId)
                    .orElseThrow(() -> new RuntimeException("User not found with ID: " + enteredByUserId));
        }

        // Find existing result or create new one
        Optional<LabTestResult> existingResult = findByLabTestOrderId(testOrderId);
        LabTestResult result;

        if (existingResult.isPresent()) {
            // Update existing result
            result = existingResult.get();
        } else {
            // Create new result
            result = new LabTestResult();
            result.setLabTestOrder(order);
            result.setCreatedAt(LocalDateTime.now());
            result.setResultDate(LocalDateTime.now());
        }

        // Set/update values
        result.setResultData(resultDataJson);
        result.setResultSummary(conclusion);
        result.setNotes(notes);
        result.setAttachmentsPath(attachmentsPath);
        result.setEnteredByUser(enteredByUser); // Set the user who entered the result
        result.setUpdatedAt(LocalDateTime.now());

        // Save and return
        return labTestResultRepository.save(result);
    }

    @Override
    public void updateOrderStatus(Long testOrderId, String status) {
        // Get the lab test order
        Optional<LabTestOrder> orderOpt = labTestOrderRepository.findById(testOrderId);
        if (orderOpt.isPresent()) {
            LabTestOrder order = orderOpt.get();
            order.setStatus(status);
            order.setUpdatedAt(LocalDateTime.now());
            labTestOrderRepository.save(order);
        } else {
            throw new RuntimeException("Lab test order not found with ID: " + testOrderId);
        }
    }

    @Override
    public List<LabTestResult> findCompleteResultsWithDetails() {
        // This method specifically fetches results for tests with status "Có kết quả"
        // First, find all test orders with status "Có kết quả"
        List<LabTestOrder> completedOrders = labTestOrderRepository.findByStatus("Có kết quả");

        // Create a list to store results with detailed information
        List<LabTestResult> detailedResults = new ArrayList<>();

        // For each completed order, retrieve the associated test result with all
        // details
        for (LabTestOrder order : completedOrders) {
            Optional<LabTestResult> resultOpt = labTestResultRepository.findByLabTestOrderId(order.getId());

            if (resultOpt.isPresent()) {
                LabTestResult result = resultOpt.get();

                // Ensure the result contains all necessary fields
                // We're returning the existing data since all fields are already in the entity:
                // - result_data (already present as resultData)
                // - result_summary (already present as resultSummary)
                // - attachments_path (already present as attachmentsPath)
                // - notes (already present as notes)
                // - created_at (already present as createdAt)
                // - updated_at (already present as updatedAt)

                detailedResults.add(result);
            }
        }

        return detailedResults;
    }

    /**
     * Convert a LabTestResult entity to a DTO with additional information
     */
    public LabTestResultDTO convertToDTO(LabTestResult result) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            LabTestOrder order = result.getLabTestOrder();
            LabTestResultDTO dto = new LabTestResultDTO();
            dto.setId(result.getId());
            dto.setLabTestOrderId(order.getId());
            dto.setTestOrderId(order.getId()); // Set testOrderId for frontend compatibility
            dto.setResultDate(result.getResultDate());
            dto.setPerformedDate(order.getOrderDate() != null ? order.getOrderDate().atStartOfDay() : null); // Convert
                                                                                                             // order
                                                                                                             // date to
                                                                                                             // LocalDateTime
            dto.setResultData(result.getResultData());
            dto.setResultSummary(result.getResultSummary());
            dto.setAttachmentsPath(result.getAttachmentsPath());
            dto.setNotes(result.getNotes());
            dto.setCreatedAt(result.getCreatedAt());
            dto.setUpdatedAt(result.getUpdatedAt());

            // Add user information
            if (result.getEnteredByUser() != null) {
                dto.setEnteredByUserId(result.getEnteredByUser().getId());
                dto.setEnteredByUserName(result.getEnteredByUser().getFullName());
            }

            // Add patient information
            if (order.getPatient() != null) {
                dto.setPatientId(order.getPatient().getId());
                dto.setPatientName(order.getPatient().getFullName());
            }

            // Add test type information
            if (order.getTestType() != null) {
                dto.setTestTypeId(order.getTestType().getId());
                dto.setTestTypeName(order.getTestType().getName());
                dto.setTestCategory(order.getTestType().getCategory());
                dto.setTestGroup(order.getTestType().getTestGroup());
            } // Add order status
            dto.setStatus(order.getStatus());

            // Add medical record ID if available
            List<MedicalRecordLabResult> medicalRecordLabResults = medicalRecordLabResultRepository
                    .findByLabTestResultId(result.getId());
            if (!medicalRecordLabResults.isEmpty()) {
                // Get the first medical record ID (assuming one-to-one relationship for this
                // use case)
                dto.setMedicalRecordId(medicalRecordLabResults.get(0).getMedicalRecord().getId());
            }

            // Parse result data if available
            if (result.getResultData() != null && !result.getResultData().isEmpty()) {
                try {
                    JsonNode resultDataNode = objectMapper.readTree(result.getResultData());
                    dto.setTestValue(resultDataNode.path("value").asText(""));
                    dto.setTestUnit(resultDataNode.path("unit").asText(""));
                    dto.setReferenceRange(resultDataNode.path("referenceRange").asText(""));
                } catch (Exception e) {
                    // Ignore parsing errors
                }
            }

            // Parse attachments if available
            if (result.getAttachmentsPath() != null && !result.getAttachmentsPath().isEmpty()) {
                dto.setParsedAttachments(result.getAttachmentsPath());
            }

            return dto;
        } catch (Exception e) {
            // Log the error but return a basic DTO
            System.err.println("Error converting result to DTO: " + e.getMessage());
            return LabTestResultDTO.builder()
                    .id(result.getId())
                    .labTestOrderId(result.getLabTestOrder().getId())
                    .testOrderId(result.getLabTestOrder().getId()) // Add testOrderId for frontend compatibility
                    .resultSummary(result.getResultSummary())
                    .build();
        }
    }

    /**
     * Convert a list of LabTestResult entities to DTOs
     */
    public List<LabTestResultDTO> convertToDTOList(List<LabTestResult> results) {
        return results.stream()
                .map(this::convertToDTO)
                .toList();
    }
}