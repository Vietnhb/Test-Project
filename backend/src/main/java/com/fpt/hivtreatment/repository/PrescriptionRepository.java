package com.fpt.hivtreatment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.fpt.hivtreatment.model.entity.Prescription;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {
        
        // Tìm đơn thuốc theo bệnh nhân
        List<Prescription> findByPatientIdOrderByCreatedAtDesc(Long patientId);
        
        // Tìm đơn thuốc theo medical record
        List<Prescription> findByMedicalRecordIdOrderByCreatedAtDesc(Long medicalRecordId);
        
        // Tìm đơn thuốc theo bác sĩ
        List<Prescription> findByDoctorIdOrderByCreatedAtDesc(Long doctorId);
        
        // Tìm đơn thuốc theo phác đồ
        List<Prescription> findByProtocolIdOrderByCreatedAtDesc(Long protocolId);        
        // Tìm đơn thuốc có thông tin chi tiết (JOIN FETCH với prescription items)
        @Query("SELECT DISTINCT p FROM Prescription p " +
                        "LEFT JOIN FETCH p.prescriptionItems pi " +
                        "LEFT JOIN FETCH pi.medication m " +
                        "LEFT JOIN FETCH p.protocol pr " +
                        "LEFT JOIN FETCH p.patient pt " +
                        "LEFT JOIN FETCH p.doctor d " +
                        "WHERE p.medicalRecordId = :medicalRecordId " +
                        "ORDER BY p.createdAt DESC")
        List<Prescription> findByMedicalRecordIdWithDetails(@Param("medicalRecordId") Long medicalRecordId);

        // Tìm đơn thuốc của bệnh nhân có thông tin chi tiết
        @Query("SELECT DISTINCT p FROM Prescription p " +
                        "LEFT JOIN FETCH p.prescriptionItems pi " +
                        "LEFT JOIN FETCH pi.medication m " +
                        "LEFT JOIN FETCH p.protocol pr " +
                        "LEFT JOIN FETCH p.doctor d " +
                        "WHERE p.patientId = :patientId " +
                        "ORDER BY p.createdAt DESC")
        List<Prescription> findByPatientIdWithDetails(@Param("patientId") Long patientId);

        // Tìm đơn thuốc đang hiệu lực của bệnh nhân
        @Query("SELECT DISTINCT p FROM Prescription p " +
                        "LEFT JOIN FETCH p.prescriptionItems pi " +
                        "LEFT JOIN FETCH pi.medication m " +
                        "WHERE p.patientId = :patientId " +
                        "AND p.treatmentStartDate <= CURRENT_DATE " +
                        "AND p.treatmentEndDate >= CURRENT_DATE " +
                        "ORDER BY p.createdAt DESC")
        List<Prescription> findActivePrescriptionsByPatientId(@Param("patientId") Long patientId);        
        // Tìm đơn thuốc theo khoảng thời gian
        @Query("SELECT p FROM Prescription p " +
                        "WHERE p.createdAt BETWEEN :startDate AND :endDate " +
                        "ORDER BY p.createdAt DESC")
        List<Prescription> findByCreatedAtBetween(@Param("startDate") LocalDate startDate,
                        @Param("endDate") LocalDate endDate);

        // Tìm đơn thuốc đang hoạt động theo khoảng thời gian
        @Query("SELECT p FROM Prescription p " +
                        "WHERE p.treatmentStartDate <= :endDate AND p.treatmentEndDate >= :startDate " +
                        "ORDER BY p.createdAt DESC")
        List<Prescription> findActivePrescriptionsBetween(@Param("startDate") LocalDate startDate,
                        @Param("endDate") LocalDate endDate);

        // Thống kê số lượng đơn thuốc theo thuốc
        @Query("SELECT pi.medicationId, COUNT(DISTINCT p) FROM Prescription p " +
                        "JOIN p.prescriptionItems pi " +
                        "WHERE p.createdAt BETWEEN :startDate AND :endDate " +
                        "GROUP BY pi.medicationId " +
                        "ORDER BY COUNT(DISTINCT p) DESC")
        List<Object[]> countPrescriptionsByMedication(@Param("startDate") LocalDate startDate,
                        @Param("endDate") LocalDate endDate);
}
