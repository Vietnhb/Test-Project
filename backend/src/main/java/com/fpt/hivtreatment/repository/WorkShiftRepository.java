package com.fpt.hivtreatment.repository;

import com.fpt.hivtreatment.model.entity.WorkShift;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkShiftRepository extends JpaRepository<WorkShift, Long> {
    List<WorkShift> findByIsActive(Boolean isActive);
}