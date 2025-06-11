package com.fpt.hivtreatment.repository;

import com.fpt.hivtreatment.model.entity.Appointment;
import com.fpt.hivtreatment.model.entity.DoctorProfile;
import com.fpt.hivtreatment.model.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
<<<<<<< HEAD
       List<Appointment> findByPatient(User patient);

       List<Appointment> findByDoctor(DoctorProfile doctor);

       List<Appointment> findByPatientAndStatus(User patient, String status);

       List<Appointment> findByDoctorAndStatus(DoctorProfile doctor, String status);

       long countByStatus(String status);

       Page<Appointment> findByStatus(String status, Pageable pageable);

       @Query("SELECT a FROM Appointment a JOIN a.appointmentSlot slot JOIN slot.doctorSchedule ds " +
                     "WHERE a.status = :status AND CAST(ds.scheduleDate AS LocalDate) BETWEEN :startDate AND :endDate")
       Page<Appointment> findByStatusAndDateRange(
                     @Param("status") String status,
                     @Param("startDate") LocalDate startDate,
                     @Param("endDate") LocalDate endDate,
                     Pageable pageable);

       /**
        * Tìm kiếm lịch hẹn đã xác nhận của bác sĩ theo ngày
        */
       @Query("SELECT a FROM Appointment a JOIN a.appointmentSlot slot JOIN slot.doctorSchedule ds " +
                     "WHERE a.doctor.doctorId = :doctorId AND a.status = :status AND ds.scheduleDate = :date")
       List<Appointment> findConfirmedAppointmentsForDoctorByDate(
                     @Param("doctorId") Long doctorId,
                     @Param("status") String status,
                     @Param("date") LocalDate date);

       /**
        * Tìm kiếm tất cả lịch hẹn của bác sĩ theo ngày (không phân biệt trạng thái)
        */
       @Query("SELECT a FROM Appointment a JOIN a.appointmentSlot slot JOIN slot.doctorSchedule ds " +
                     "WHERE a.doctor.doctorId = :doctorId AND ds.scheduleDate = :date")
       List<Appointment> findAppointmentsForDoctorByDate(
                     @Param("doctorId") Long doctorId,
                     @Param("date") LocalDate date);

       /**
        * Tìm kiếm lịch hẹn theo loại (ví dụ: "Tư vấn") và trạng thái
        */
       @Query("SELECT a FROM Appointment a JOIN a.appointmentSlot slot JOIN slot.doctorSchedule ds " +
                     "WHERE a.doctor.doctorId = :doctorId AND a.appointmentType = :type " +
                     "AND a.status = :status AND ds.scheduleDate = :date")
       List<Appointment> findAppointmentsByTypeAndStatusForDoctorByDate(
                     @Param("doctorId") Long doctorId,
                     @Param("type") String type,
                     @Param("status") String status,
                     @Param("date") LocalDate date);

       /**
        * Tìm kiếm lịch hẹn loại trừ một loại nhất định (ví dụ: không phải "Tư vấn") và
        * theo trạng thái
        */
       @Query("SELECT a FROM Appointment a JOIN a.appointmentSlot slot JOIN slot.doctorSchedule ds " +
                     "WHERE a.doctor.doctorId = :doctorId AND a.appointmentType != :excludeType " +
                     "AND a.status = :status AND ds.scheduleDate = :date")
       List<Appointment> findAppointmentsExcludeTypeAndStatusForDoctorByDate(
                     @Param("doctorId") Long doctorId,
                     @Param("excludeType") String excludeType,
                     @Param("status") String status,
                     @Param("date") LocalDate date);
=======
    List<Appointment> findByPatient(User patient);

    List<Appointment> findByDoctor(DoctorProfile doctor);

    List<Appointment> findByPatientAndStatus(User patient, String status);

    List<Appointment> findByDoctorAndStatus(DoctorProfile doctor, String status);
    
    long countByStatus(String status);
    
    Page<Appointment> findByStatus(String status, Pageable pageable);
    
    @Query("SELECT a FROM Appointment a JOIN a.appointmentSlot slot JOIN slot.doctorSchedule ds " +
           "WHERE a.status = :status AND CAST(ds.scheduleDate AS LocalDate) BETWEEN :startDate AND :endDate")
    Page<Appointment> findByStatusAndDateRange(
            @Param("status") String status, 
            @Param("startDate") LocalDate startDate, 
            @Param("endDate") LocalDate endDate, 
            Pageable pageable);
>>>>>>> fd42c148e0431975301ca683137e9cc7dea64a1c
}