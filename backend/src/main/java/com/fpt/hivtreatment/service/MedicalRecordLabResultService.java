package com.fpt.hivtreatment.service;

import com.fpt.hivtreatment.model.entity.LabTestResult;
import com.fpt.hivtreatment.model.entity.MedicalRecord;
import com.fpt.hivtreatment.model.entity.MedicalRecordLabResult;

import java.util.List;
import java.util.Optional;

public interface MedicalRecordLabResultService {

    /**
     * Create a new association between a medical record and a lab test result
     */
    MedicalRecordLabResult createAssociation(Long medicalRecordId, Long labTestResultId, String resultInterpretation,
            String clinicalSignificance);

    /**
     * Find all associations for a medical record
     */
    List<MedicalRecordLabResult> findByMedicalRecordId(Long medicalRecordId);

    /**
     * Find all associations for a lab test result
     */
    List<MedicalRecordLabResult> findByLabTestResultId(Long labTestResultId);

    /**
     * Find a specific association by medical record ID and lab test result ID
     */
    Optional<MedicalRecordLabResult> findByMedicalRecordIdAndLabTestResultId(Long medicalRecordId,
            Long labTestResultId);

    /**
     * Update the interpretation and significance for an association
     */
    MedicalRecordLabResult updateInterpretation(Long medicalRecordId, Long labTestResultId, String resultInterpretation,
            String clinicalSignificance);

    /**
     * Find all lab test results associated with a medical record
     */
    List<LabTestResult> findLabTestResultsByMedicalRecordId(Long medicalRecordId);

    /**
     * Find all medical records associated with a lab test result
     */
    List<MedicalRecord> findMedicalRecordsByLabTestResultId(Long labTestResultId);

    /**
     * Delete an association between a medical record and a lab test result
     */
    void deleteAssociation(Long medicalRecordId, Long labTestResultId);

    /**
     * Delete all associations for a medical record
     */
    void deleteAllByMedicalRecordId(Long medicalRecordId);

    /**
     * Delete all associations for a lab test result
     */
    void deleteAllByLabTestResultId(Long labTestResultId);
}