package com.fpt.hivtreatment.repository;

import com.fpt.hivtreatment.model.entity.MedicationReminder;
import com.fpt.hivtreatment.model.entity.MedicationReminder.ReminderType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface MedicationReminderRepository extends JpaRepository<MedicationReminder, Long> {

    /**
     * Tìm tất cả reminders cho một bệnh nhân
     */
    List<MedicationReminder> findByPatientId(Long patientId);

    /**
     * Tìm active reminders cho một bệnh nhân
     */
    List<MedicationReminder> findByPatientIdAndIsActive(Long patientId, Boolean isActive);

    /**
     * Tìm reminders theo prescription item
     */
    List<MedicationReminder> findByPrescriptionItemId(Long prescriptionItemId);

    /**
     * Tìm reminders cần gửi trong ngày hôm nay
     */
    @Query("SELECT mr FROM MedicationReminder mr " +
           "WHERE mr.isActive = true " +
           "AND :currentDate BETWEEN mr.startDate AND mr.endDate " +
           "ORDER BY mr.reminderTime")
    List<MedicationReminder> findTodayReminders(@Param("currentDate") LocalDate currentDate);

    /**
     * Tìm reminders cần gửi cho một bệnh nhân trong ngày
     */
    @Query("SELECT mr FROM MedicationReminder mr " +
           "WHERE mr.patientId = :patientId " +
           "AND mr.isActive = true " +
           "AND :currentDate BETWEEN mr.startDate AND mr.endDate " +
           "ORDER BY mr.reminderTime")
    List<MedicationReminder> findTodayRemindersByPatient(
            @Param("patientId") Long patientId, 
            @Param("currentDate") LocalDate currentDate);

    /**
     * Tìm reminders theo thời gian và loại
     */
    @Query("SELECT mr FROM MedicationReminder mr " +
           "WHERE mr.isActive = true " +
           "AND :currentDate BETWEEN mr.startDate AND mr.endDate " +
           "AND mr.reminderType = :reminderType " +
           "AND mr.reminderTime = :reminderTime")
    List<MedicationReminder> findByTimeAndType(
            @Param("currentDate") LocalDate currentDate,
            @Param("reminderType") ReminderType reminderType,
            @Param("reminderTime") LocalTime reminderTime);

    /**
     * Xóa tất cả reminders của một prescription item
     */
    void deleteByPrescriptionItemId(Long prescriptionItemId);

    /**
     * Đếm số reminders active cho một bệnh nhân
     */
    @Query("SELECT COUNT(mr) FROM MedicationReminder mr " +
           "WHERE mr.patientId = :patientId " +
           "AND mr.isActive = true " +
           "AND CURRENT_DATE BETWEEN mr.startDate AND mr.endDate")
    Long countActiveRemindersByPatient(@Param("patientId") Long patientId);
}
