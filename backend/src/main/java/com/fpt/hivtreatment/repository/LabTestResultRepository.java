package com.fpt.hivtreatment.repository;

import com.fpt.hivtreatment.model.entity.LabTestOrder;
import com.fpt.hivtreatment.model.entity.LabTestResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LabTestResultRepository extends JpaRepository<LabTestResult, Long> {

    /**
     * Check if a lab test result exists for a given lab test order ID
     */
    boolean existsByLabTestOrderId(Long labTestOrderId);

    /**
     * Find result by lab test order
     */
    Optional<LabTestResult> findByLabTestOrder(LabTestOrder labTestOrder);

    /**
     * Find result by lab test order ID
     */
    Optional<LabTestResult> findByLabTestOrderId(Long labTestOrderId);

    /**
     * Find all results for a specific patient
     */
    @Query("SELECT lr FROM LabTestResult lr JOIN lr.labTestOrder o WHERE o.patient.id = :patientId ORDER BY lr.resultDate DESC")
    List<LabTestResult> findByPatientId(@Param("patientId") Long patientId);

    /**
     * Find all results for a specific doctor
     */
    @Query("SELECT lr FROM LabTestResult lr JOIN lr.labTestOrder o WHERE o.doctor.id = :doctorId ORDER BY lr.resultDate DESC")
    List<LabTestResult> findByDoctorId(@Param("doctorId") Long doctorId);

    /**
     * Find all results for a specific test type
     */
    @Query("SELECT lr FROM LabTestResult lr JOIN lr.labTestOrder o WHERE o.testType.id = :testTypeId ORDER BY lr.resultDate DESC")
    List<LabTestResult> findByTestTypeId(@Param("testTypeId") Long testTypeId);

    /**
     * Find all results by patient and test type
     */
    @Query("SELECT lr FROM LabTestResult lr JOIN lr.labTestOrder o WHERE o.patient.id = :patientId AND o.testType.id = :testTypeId ORDER BY lr.resultDate DESC")
    List<LabTestResult> findByPatientIdAndTestTypeId(@Param("patientId") Long patientId,
            @Param("testTypeId") Long testTypeId);

    /**
     * Find latest result for a patient and test type
     */
    @Query("SELECT lr FROM LabTestResult lr JOIN lr.labTestOrder o WHERE o.patient.id = :patientId AND o.testType.id = :testTypeId ORDER BY lr.resultDate DESC")
    List<LabTestResult> findLatestByPatientIdAndTestTypeId(@Param("patientId") Long patientId,
            @Param("testTypeId") Long testTypeId);

    /**
     * Count results with attachments
     */
    @Query("SELECT COUNT(lr) FROM LabTestResult lr WHERE lr.attachmentsPath IS NOT NULL AND lr.attachmentsPath <> ''")
    Long countResultsWithAttachments();
}