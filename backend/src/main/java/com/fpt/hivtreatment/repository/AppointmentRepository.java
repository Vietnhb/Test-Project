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
}