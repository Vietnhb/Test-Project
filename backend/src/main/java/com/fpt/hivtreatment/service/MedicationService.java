package com.fpt.hivtreatment.service;

import com.fpt.hivtreatment.model.entity.Medication;

import java.util.List;
import java.util.Optional;

public interface MedicationService {
    List<Medication> getAllMedications();

    List<Medication> getActiveMedications();

    Optional<Medication> getMedicationById(Long id);

    List<Medication> getMedicationsByCategory(String category);

    Medication saveMedication(Medication medication);

    void deleteMedication(Long id);
}