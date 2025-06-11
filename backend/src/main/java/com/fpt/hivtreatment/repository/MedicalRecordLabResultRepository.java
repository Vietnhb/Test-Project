package com.fpt.hivtreatment.repository;

import com.fpt.hivtreatment.model.entity.LabTestResult;
import com.fpt.hivtreatment.model.entity.MedicalRecord;
import com.fpt.hivtreatment.model.entity.MedicalRecordLabResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MedicalRecordLabResultRepository extends JpaRepository<MedicalRecordLabResult, Long> {

    /**
     * Find all links for a specific medical record
     */
    List<MedicalRecordLabResult> findByMedicalRecord(MedicalRecord medicalRecord);

    /**
     * Find all links for a medical record by ID
     */
    List<MedicalRecordLabResult> findByMedicalRecordId(Long medicalRecordId);

    /**
     * Find all links for a lab test result
     */
    List<MedicalRecordLabResult> findByLabTestResult(LabTestResult labTestResult);

    /**
     * Find all links for a lab test result by ID
     */
    List<MedicalRecordLabResult> findByLabTestResultId(Long labTestResultId);

    /**
     * Find a specific link by medical record ID and lab test result ID
     */
    Optional<MedicalRecordLabResult> findByMedicalRecordIdAndLabTestResultId(Long medicalRecordId,
            Long labTestResultId);

    /**
     * Check if a link exists between a medical record and lab test result
     */
    boolean existsByMedicalRecordIdAndLabTestResultId(Long medicalRecordId, Long labTestResultId);

    /**
     * Find all lab test results associated with a medical record
     */
    @Query("SELECT mrlr.labTestResult FROM MedicalRecordLabResult mrlr WHERE mrlr.medicalRecord.id = :medicalRecordId ORDER BY mrlr.labTestResult.resultDate DESC")
    List<LabTestResult> findLabTestResultsByMedicalRecordId(@Param("medicalRecordId") Long medicalRecordId);

    /**
     * Find all medical records associated with a lab test result
     */
    @Query("SELECT mrlr.medicalRecord FROM MedicalRecordLabResult mrlr WHERE mrlr.labTestResult.id = :labTestResultId ORDER BY mrlr.medicalRecord.visitDate DESC")
    List<MedicalRecord> findMedicalRecordsByLabTestResultId(@Param("labTestResultId") Long labTestResultId);
}