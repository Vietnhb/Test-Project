package com.fpt.hivtreatment.service;

import com.fpt.hivtreatment.model.entity.LabTestResult;
import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

public interface LabTestResultService {

        /**
         * Save a new lab test result
         */
        LabTestResult saveLabTestResult(LabTestResult labTestResult);

        /**
         * Update an existing lab test result
         */
        LabTestResult updateLabTestResult(Long id, LabTestResult labTestResult);

        /**
         * Find a lab test result by ID
         */
        Optional<LabTestResult> findById(Long id);

        /**
         * Find a lab test result by lab test order ID
         */
        Optional<LabTestResult> findByLabTestOrderId(Long labTestOrderId);

        /**
         * Find all lab test results for a patient
         */
        List<LabTestResult> findByPatientId(Long patientId);

        /**
         * Find all lab test results for a doctor
         */
        List<LabTestResult> findByDoctorId(Long doctorId);

        /**
         * Find all lab test results for a test type
         */
        List<LabTestResult> findByTestTypeId(Long testTypeId);

        /**
         * Find all lab test results for a patient and test type
         */
        List<LabTestResult> findByPatientIdAndTestTypeId(Long patientId, Long testTypeId);

        /**
         * Find the latest lab test result for a patient and test type
         */
        Optional<LabTestResult> findLatestByPatientIdAndTestTypeId(Long patientId, Long testTypeId);

        /**
         * Update the attachments path for a lab test result
         */
        LabTestResult updateAttachmentsPath(Long id, String attachmentsPath);

        /**
         * Delete a lab test result
         */
        void deleteLabTestResult(Long id);

        /**
         * Associate a lab test result with a medical record
         */
        void associateWithMedicalRecord(Long labTestResultId, Long medicalRecordId, String resultInterpretation,
                        String clinicalSignificance);

        /**
         * Get all lab test results
         */
        List<LabTestResult> findAll();

        /**
         * Create or update a lab test result for a specific test order
         */
        LabTestResult createOrUpdateLabTestResult(
                        Long testOrderId,
                        String value,
                        String unit,
                        String referenceRange,
                        String notes,
                        String conclusion,
                        String status,
                        String attachmentsPath,
                        Long enteredByUserId);

        /**
         * Create or update a lab test result for a specific test order with JSON data
         */
        LabTestResult createOrUpdateLabTestResult(
                        Long testOrderId,
                        String resultDataJson,
                        String conclusion,
                        String notes,
                        String attachmentsPath,
                        Long enteredByUserId);

        /**
         * Update the status of a lab test order
         */
        void updateOrderStatus(Long testOrderId, String status);

        /**
         * Find all lab test results for tests with status "Có kết quả" with complete
         * details
         * 
         * @return List of LabTestResult objects with all details
         */
        List<LabTestResult> findCompleteResultsWithDetails();
}