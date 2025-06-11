package com.fpt.hivtreatment.repository;

import com.fpt.hivtreatment.model.entity.DoctorProfile;
import com.fpt.hivtreatment.model.entity.DoctorSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface DoctorScheduleRepository extends JpaRepository<DoctorSchedule, Long> {
    List<DoctorSchedule> findByDoctor(DoctorProfile doctor);

    List<DoctorSchedule> findByDoctorAndScheduleDate(DoctorProfile doctor, LocalDate scheduleDate);

    List<DoctorSchedule> findByScheduleDateGreaterThanEqual(LocalDate date);

    List<DoctorSchedule> findByScheduleDate(LocalDate date);

    List<DoctorSchedule> findByDoctorAndScheduleDateBetween(DoctorProfile doctor, LocalDate startDate,
            LocalDate endDate);

    long countByDoctorAndScheduleDate(DoctorProfile doctor, LocalDate scheduleDate);

    long countByDoctor(DoctorProfile doctor);

    long countByScheduleDate(LocalDate date);

    List<DoctorSchedule> findByScheduleDateBetween(LocalDate startDate, LocalDate endDate);
}