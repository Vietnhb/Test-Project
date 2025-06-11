package com.fpt.hivtreatment.service;

import com.fpt.hivtreatment.model.entity.MedicationReminder;
import java.time.LocalDateTime;
import java.util.List;

public interface MedicationReminderService {

    // Tạo medication reminder mới
    MedicationReminder createMedicationReminder(MedicationReminder medicationReminder);

    // Tạo nhiều medication reminders cùng lúc
    List<MedicationReminder> createMedicationReminders(List<MedicationReminder> medicationReminders);

    // Tạo reminders tự động từ prescription items
    List<MedicationReminder> createRemindersFromPrescriptionItems(List<Long> prescriptionItemIds);

    // Cập nhật medication reminder
    MedicationReminder updateMedicationReminder(Long id, MedicationReminder medicationReminder);

    // Xóa medication reminder
    void deleteMedicationReminder(Long id);

    // Tìm medication reminder theo ID
    MedicationReminder findById(Long id);

    // Tìm medication reminders theo bệnh nhân
    List<MedicationReminder> findByPatientId(Long patientId);

    // Tìm medication reminders theo prescription item
    List<MedicationReminder> findByPrescriptionItemId(Long prescriptionItemId);

    // Tìm medication reminders theo thời gian
    List<MedicationReminder> findByReminderTime(LocalDateTime reminderTime);

    // Tìm medication reminders theo loại (morning, noon, afternoon, evening)
    List<MedicationReminder> findByTimeSlot(String timeSlot);

    // Tìm medication reminders đang hoạt động theo bệnh nhân
    List<MedicationReminder> findActiveByPatientId(Long patientId);

    // Cập nhật trạng thái reminder
    MedicationReminder updateStatus(Long id, String status);

    // Tìm reminders cần gửi trong khoảng thời gian
    List<MedicationReminder> findRemindersToSend(LocalDateTime startTime, LocalDateTime endTime);

    // Đánh dấu reminder đã được gửi
    MedicationReminder markAsSent(Long id);

    // Kích hoạt/tắt medication reminder
    MedicationReminder toggleReminder(Long id, boolean active);
}
