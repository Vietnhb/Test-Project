package com.fpt.hivtreatment.repository;

import com.fpt.hivtreatment.model.entity.AppointmentSlot;
import com.fpt.hivtreatment.model.entity.DoctorProfile;
import com.fpt.hivtreatment.model.entity.DoctorSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AppointmentSlotRepository extends JpaRepository<AppointmentSlot, Long> {
    List<AppointmentSlot> findByDoctorScheduleId(Long doctorScheduleId);

    List<AppointmentSlot> findByDoctorSchedule(DoctorSchedule doctorSchedule);

    List<AppointmentSlot> findByDoctor(DoctorProfile doctor);

    List<AppointmentSlot> findByDoctorAndDoctorSchedule_ScheduleDate(DoctorProfile doctor, LocalDate date);

    void deleteByDoctorScheduleId(Long doctorScheduleId);
}