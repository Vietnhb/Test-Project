package com.fpt.hivtreatment.service;

import com.fpt.hivtreatment.model.entity.Reminder;
import java.time.LocalDateTime;
import java.util.List;

public interface ReminderService {

    // Tạo reminder mới
    Reminder createReminder(Reminder reminder);

    // Tạo nhiều reminders cùng lúc
    List<Reminder> createReminders(List<Reminder> reminders);

    // Cập nhật reminder
    Reminder updateReminder(Long id, Reminder reminder);

    // Xóa reminder
    void deleteReminder(Long id);

    // Tìm reminder theo ID
    Reminder findById(Long id);

    // Tìm reminders theo bệnh nhân
    List<Reminder> findByPatientId(Long patientId);

    // Tìm reminders theo loại (medication, appointment)
    List<Reminder> findByType(String type);

    // Tìm reminders theo thời gian
    List<Reminder> findByReminderTime(LocalDateTime reminderTime);

    // Tìm reminders đang hoạt động theo bệnh nhân
    List<Reminder> findActiveByPatientId(Long patientId);

    // Tìm reminders theo bệnh nhân và loại
    List<Reminder> findByPatientIdAndType(Long patientId, String type);

    // Cập nhật trạng thái reminder
    Reminder updateStatus(Long id, String status);

    // Tìm reminders cần gửi trong khoảng thời gian
    List<Reminder> findRemindersToSend(LocalDateTime startTime, LocalDateTime endTime);

    // Đánh dấu reminder đã được gửi
    Reminder markAsSent(Long id);

    // Tìm reminders theo trạng thái
    List<Reminder> findByStatus(String status);

    // Tìm reminders đang hoạt động theo bệnh nhân và ngày
    List<Reminder> findActiveRemindersByDate(Long patientId, java.time.LocalDate date);
}
