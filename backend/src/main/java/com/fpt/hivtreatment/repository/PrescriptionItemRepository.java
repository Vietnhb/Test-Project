package com.fpt.hivtreatment.repository;

import com.fpt.hivtreatment.model.entity.PrescriptionItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PrescriptionItemRepository extends JpaRepository<PrescriptionItem, Long> {

    /**
     * Tìm tất cả prescription items theo prescription ID
     */
    List<PrescriptionItem> findByPrescriptionId(Long prescriptionId);

    /**
     * Tìm tất cả prescription items theo medication ID
     */
    List<PrescriptionItem> findByMedicationId(Long medicationId);

    /**
     * Tìm prescription items theo patient ID (thông qua prescription)
     */
    @Query("SELECT pi FROM PrescriptionItem pi " +
           "JOIN pi.prescription p " +
           "WHERE p.patientId = :patientId")
    List<PrescriptionItem> findByPatientId(@Param("patientId") Long patientId);

    /**
     * Tìm prescription items đang active theo patient ID
     */
    @Query("SELECT pi FROM PrescriptionItem pi " +
           "JOIN pi.prescription p " +
           "WHERE p.patientId = :patientId " +
           "AND p.status IN ('Đã kê', 'Đã cấp phát') " +
           "AND CURRENT_DATE BETWEEN p.treatmentStartDate AND p.treatmentEndDate")
    List<PrescriptionItem> findActiveByPatientId(@Param("patientId") Long patientId);

    /**
     * Kiểm tra xem medication đã có trong prescription chưa
     */
    boolean existsByPrescriptionIdAndMedicationId(Long prescriptionId, Long medicationId);

    /**
     * Tính tổng số lượng thuốc theo prescription ID
     */
    @Query("SELECT SUM(pi.totalQuantity) FROM PrescriptionItem pi WHERE pi.prescriptionId = :prescriptionId")
    Integer getTotalQuantityByPrescriptionId(@Param("prescriptionId") Long prescriptionId);
}
