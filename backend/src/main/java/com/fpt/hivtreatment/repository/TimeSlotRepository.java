package com.fpt.hivtreatment.repository;

import com.fpt.hivtreatment.model.entity.TimeSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalTime;
import java.util.List;

@Repository
public interface TimeSlotRepository extends JpaRepository<TimeSlot, Long> {
    List<TimeSlot> findByStartTimeGreaterThanEqualAndEndTimeLessThanEqual(LocalTime startTime, LocalTime endTime);
}