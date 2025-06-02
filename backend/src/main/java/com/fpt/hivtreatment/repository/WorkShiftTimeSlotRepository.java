package com.fpt.hivtreatment.repository;

import com.fpt.hivtreatment.model.entity.TimeSlot;
import com.fpt.hivtreatment.model.entity.WorkShift;
import com.fpt.hivtreatment.model.entity.WorkShiftTimeSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkShiftTimeSlotRepository extends JpaRepository<WorkShiftTimeSlot, Long> {
    List<WorkShiftTimeSlot> findByWorkShift(WorkShift workShift);

    List<WorkShiftTimeSlot> findByTimeSlot(TimeSlot timeSlot);

    List<WorkShiftTimeSlot> findByWorkShiftId(Long workShiftId);
}