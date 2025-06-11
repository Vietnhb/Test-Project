package com.fpt.hivtreatment.repository;

import com.fpt.hivtreatment.model.entity.MedicalRecord;
import com.fpt.hivtreatment.model.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, Long> {

    List<MedicalRecord> findByPatient(User patient);

    List<MedicalRecord> findByDoctor(User doctor);

    Page<MedicalRecord> findByPatient(User patient, Pageable pageable);

    Page<MedicalRecord> findByDoctor(User doctor, Pageable pageable);

    List<MedicalRecord> findByPatientAndRecordStatus(User patient, String status);

    List<MedicalRecord> findByDoctorAndRecordStatus(User doctor, String status);

    @Query("SELECT mr FROM MedicalRecord mr WHERE mr.patient.id = :patientId AND mr.visitDate BETWEEN :startDate AND :endDate ORDER BY mr.visitDate DESC")
    List<MedicalRecord> findByPatientIdAndDateRange(
            @Param("patientId") Long patientId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT mr FROM MedicalRecord mr WHERE mr.doctor.id = :doctorId AND mr.visitDate = :date")
    List<MedicalRecord> findByDoctorIdAndDate(
            @Param("doctorId") Long doctorId,
            @Param("date") LocalDate date);

    @Query("SELECT COUNT(mr) FROM MedicalRecord mr WHERE mr.recordStatus = :status")
    long countByRecordStatus(@Param("status") String status);
}