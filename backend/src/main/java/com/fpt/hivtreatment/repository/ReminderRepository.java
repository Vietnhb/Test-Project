package com.fpt.hivtreatment.repository;

import com.fpt.hivtreatment.model.entity.Reminder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ReminderRepository extends JpaRepository<Reminder, Long> {

    /**
     * Tìm tất cả reminders cho một bệnh nhân
     */
    List<Reminder> findByPatientId(Long patientId);

    /**
     * Tìm active reminders cho một bệnh nhân
     */
    List<Reminder> findByPatientIdAndIsActive(Long patientId, Boolean isActive);

    /**
     * Tìm reminders theo loại
     */
    List<Reminder> findByReminderType(String reminderType);

    /**
     * Tìm reminders theo prescription ID
     */
    List<Reminder> findByPrescriptionId(Long prescriptionId);

    /**
     * Tìm reminders theo medical record ID
     */
    List<Reminder> findByMedicalRecordId(Long medicalRecordId);

    /**
     * Tìm appointment reminders cần gửi
     */
    @Query("SELECT r FROM Reminder r " +
           "WHERE r.reminderType = 'APPOINTMENT' " +
           "AND r.isActive = true " +
           "AND r.reminderDate = :reminderDate")
    List<Reminder> findAppointmentRemindersForDate(@Param("reminderDate") LocalDate reminderDate);

    /**
     * Tìm medication reminders đang active
     */
    @Query("SELECT r FROM Reminder r " +
           "WHERE r.reminderType = 'MEDICATION' " +
           "AND r.isActive = true " +
           "AND :currentDate BETWEEN r.startDate AND COALESCE(r.endDate, :currentDate)")
    List<Reminder> findActiveMedicationReminders(@Param("currentDate") LocalDate currentDate);

    /**
     * Tìm reminders cần gửi hôm nay cho một bệnh nhân
     */
    @Query("SELECT r FROM Reminder r " +
           "WHERE r.patientId = :patientId " +
           "AND r.isActive = true " +
           "AND ((r.reminderType = 'MEDICATION' AND :currentDate BETWEEN r.startDate AND COALESCE(r.endDate, :currentDate)) " +
           "     OR (r.reminderType = 'APPOINTMENT' AND r.reminderDate = :currentDate))")
    List<Reminder> findTodayRemindersByPatient(
            @Param("patientId") Long patientId, 
            @Param("currentDate") LocalDate currentDate);

    /**
     * Deactivate reminders khi hoàn thành điều trị
     */
    @Query("UPDATE Reminder r SET r.isActive = false " +
           "WHERE r.prescriptionId = :prescriptionId")
    void deactivateByPrescriptionId(@Param("prescriptionId") Long prescriptionId);
}
