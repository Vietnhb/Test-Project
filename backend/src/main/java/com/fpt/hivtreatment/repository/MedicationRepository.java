package com.fpt.hivtreatment.repository;

import com.fpt.hivtreatment.model.entity.Medication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MedicationRepository extends JpaRepository<Medication, Long> {
    List<Medication> findByCategory(String category);

    List<Medication> findByIsActiveTrue();
}