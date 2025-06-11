package com.fpt.hivtreatment.service.impl;

import com.fpt.hivtreatment.model.entity.Medication;
import com.fpt.hivtreatment.repository.MedicationRepository;
import com.fpt.hivtreatment.service.MedicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MedicationServiceImpl implements MedicationService {

    private final MedicationRepository medicationRepository;

    @Autowired
    public MedicationServiceImpl(MedicationRepository medicationRepository) {
        this.medicationRepository = medicationRepository;
    }

    @Override
    public List<Medication> getAllMedications() {
        return medicationRepository.findAll();
    }

    @Override
    public List<Medication> getActiveMedications() {
        return medicationRepository.findByIsActiveTrue();
    }

    @Override
    public Optional<Medication> getMedicationById(Long id) {
        return medicationRepository.findById(id);
    }

    @Override
    public List<Medication> getMedicationsByCategory(String category) {
        return medicationRepository.findByCategory(category);
    }

    @Override
    public Medication saveMedication(Medication medication) {
        return medicationRepository.save(medication);
    }

    @Override
    public void deleteMedication(Long id) {
        medicationRepository.deleteById(id);
    }
}