package com.fpt.hivtreatment.service;

import java.time.LocalDate;
import java.util.List;

import com.fpt.hivtreatment.model.entity.Prescription;
import com.fpt.hivtreatment.model.entity.PrescriptionItem;
import com.fpt.hivtreatment.model.entity.MedicationReminder;

public interface PrescriptionService {

    // Tạo đơn thuốc mới (prescription header)
    Prescription createPrescription(Prescription prescription);

    // Tạo đơn thuốc đầy đủ với prescription items và medication reminders
    Prescription createFullPrescription(Prescription prescription, List<PrescriptionItem> prescriptionItems);

    // Cập nhật đơn thuốc
    Prescription updatePrescription(Long id, Prescription prescription);

    // Xóa đơn thuốc (cascade xóa prescription items và medication reminders)
    void deletePrescription(Long id);

    // Tìm đơn thuốc theo ID
    Prescription findById(Long id);

    // Tìm đơn thuốc theo bệnh nhân
    List<Prescription> findByPatientId(Long patientId);

    // Tìm đơn thuốc theo medical record
    List<Prescription> findByMedicalRecordId(Long medicalRecordId);

    // Tìm đơn thuốc có thông tin chi tiết theo medical record
    List<Prescription> findByMedicalRecordIdWithDetails(Long medicalRecordId);

    // Tìm đơn thuốc có thông tin chi tiết theo bệnh nhân
    List<Prescription> findByPatientIdWithDetails(Long patientId);

    // Tìm đơn thuốc đang hiệu lực của bệnh nhân
    List<Prescription> findActivePrescriptionsByPatientId(Long patientId);

    // Tìm đơn thuốc theo khoảng thời gian
    List<Prescription> findByDateRange(LocalDate startDate, LocalDate endDate);

    // Tìm đơn thuốc đang hoạt động trong khoảng thời gian
    List<Prescription> findActivePrescriptionsBetween(LocalDate startDate, LocalDate endDate);

    // Thống kê đơn thuốc theo thuốc
    List<Object[]> getPrescriptionStatsByMedication(LocalDate startDate, LocalDate endDate);

    // Thêm prescription item vào đơn thuốc hiện có
    PrescriptionItem addPrescriptionItem(Long prescriptionId, PrescriptionItem prescriptionItem);

    // Xóa prescription item khỏi đơn thuốc
    void removePrescriptionItem(Long prescriptionId, Long prescriptionItemId);

    // Tạo medication reminders tự động cho đơn thuốc
    List<MedicationReminder> generateMedicationReminders(Long prescriptionId);

    // Cập nhật trạng thái đơn thuốc
    Prescription updatePrescriptionStatus(Long id, String status);

    // Hủy đơn thuốc
    Prescription cancelPrescription(Long id);

    // Hoàn thành đơn thuốc
    Prescription completePrescription(Long id);
}
