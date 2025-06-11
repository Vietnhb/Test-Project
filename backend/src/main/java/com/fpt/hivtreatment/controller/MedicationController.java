package com.fpt.hivtreatment.controller;

import com.fpt.hivtreatment.model.entity.Medication;
import com.fpt.hivtreatment.service.MedicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/medications")
public class MedicationController {

    private final MedicationService medicationService;

    @Autowired
    public MedicationController(MedicationService medicationService) {
        this.medicationService = medicationService;
    }

    @GetMapping
    public ResponseEntity<List<Medication>> getAllMedications() {
        List<Medication> medications = medicationService.getAllMedications();
        return new ResponseEntity<>(medications, HttpStatus.OK);
    }

    @GetMapping("/active")
    public ResponseEntity<List<Medication>> getActiveMedications() {
        List<Medication> medications = medicationService.getActiveMedications();
        return new ResponseEntity<>(medications, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Medication> getMedicationById(@PathVariable Long id) {
        Optional<Medication> medication = medicationService.getMedicationById(id);
        return medication.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<Medication>> getMedicationsByCategory(@PathVariable String category) {
        List<Medication> medications = medicationService.getMedicationsByCategory(category);
        return new ResponseEntity<>(medications, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<Medication> createMedication(@RequestBody Medication medication) {
        Medication savedMedication = medicationService.saveMedication(medication);
        return new ResponseEntity<>(savedMedication, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Medication> updateMedication(
            @PathVariable Long id, @RequestBody Medication medication) {
        Optional<Medication> existingMedication = medicationService.getMedicationById(id);
        if (existingMedication.isPresent()) {
            medication.setId(id);
            Medication updatedMedication = medicationService.saveMedication(medication);
            return new ResponseEntity<>(updatedMedication, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMedication(@PathVariable Long id) {
        Optional<Medication> existingMedication = medicationService.getMedicationById(id);
        if (existingMedication.isPresent()) {
            medicationService.deleteMedication(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}