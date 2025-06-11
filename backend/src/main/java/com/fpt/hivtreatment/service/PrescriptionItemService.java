package com.fpt.hivtreatment.service;

import com.fpt.hivtreatment.model.entity.PrescriptionItem;
import java.util.List;

public interface PrescriptionItemService {

    // Tạo prescription item mới
    PrescriptionItem createPrescriptionItem(PrescriptionItem prescriptionItem);

    // Tạo nhiều prescription items cùng lúc
    List<PrescriptionItem> createPrescriptionItems(List<PrescriptionItem> prescriptionItems);

    // Cập nhật prescription item
    PrescriptionItem updatePrescriptionItem(Long id, PrescriptionItem prescriptionItem);

    // Xóa prescription item
    void deletePrescriptionItem(Long id);

    // Tìm prescription item theo ID
    PrescriptionItem findById(Long id);

    // Tìm prescription items theo prescription
    List<PrescriptionItem> findByPrescriptionId(Long prescriptionId);

    // Tìm prescription items theo bệnh nhân
    List<PrescriptionItem> findByPatientId(Long patientId);

    // Tìm prescription items theo thuốc
    List<PrescriptionItem> findByMedicationId(Long medicationId);

    // Tìm prescription items đang hoạt động theo bệnh nhân
    List<PrescriptionItem> findActiveByPatientId(Long patientId);

    // Tìm prescription items đang hoạt động theo thuốc
    List<PrescriptionItem> findActiveByMedicationId(Long medicationId);

    // Cập nhật trạng thái prescription item
    PrescriptionItem updateStatus(Long id, String status);    // Tính tổng liều hàng ngày cho prescription item
    Integer calculateDailyTotal(PrescriptionItem prescriptionItem);
}
