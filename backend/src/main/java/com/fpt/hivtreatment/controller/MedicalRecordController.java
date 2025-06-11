package com.fpt.hivtreatment.controller;

import com.fpt.hivtreatment.dto.MedicalRecordDTO;
import com.fpt.hivtreatment.exception.ResourceNotFoundException;
import com.fpt.hivtreatment.service.MedicalRecordService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller for managing medical records
 */
@RestController
@RequestMapping("/api/medical-records")
@CrossOrigin(origins = "*")
public class MedicalRecordController {

    @Autowired
    private MedicalRecordService medicalRecordService;

    /**
     * Create a new medical record
     */
    @PostMapping
    @PreAuthorize("hasAuthority('2') or hasAuthority('3') or hasAuthority('4') or hasAuthority('5')") // Doctor, Staff,
                                                                                                      // Admin, Manager
    public ResponseEntity<?> createMedicalRecord(@RequestBody MedicalRecordDTO medicalRecordDTO) {
        try {
            MedicalRecordDTO createdRecord = medicalRecordService.createMedicalRecord(medicalRecordDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdRecord);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error creating medical record: " + e.getMessage()));
        }
    }

    /**
     * Get a medical record by ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('1') or hasAuthority('2') or hasAuthority('3') or hasAuthority('4') or hasAuthority('5')")
    public ResponseEntity<?> getMedicalRecordById(@PathVariable Long id) {
        try {
            MedicalRecordDTO record = medicalRecordService.getMedicalRecordById(id);
            return ResponseEntity.ok(record);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error retrieving medical record: " + e.getMessage()));
        }
    }

    /**
     * Update an existing medical record
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('2') or hasAuthority('3') or hasAuthority('4') or hasAuthority('5')") // Doctor, Staff,
                                                                                                      // Admin, Manager
    public ResponseEntity<?> updateMedicalRecord(@PathVariable Long id,
            @RequestBody MedicalRecordDTO medicalRecordDTO) {
        try {
            MedicalRecordDTO updatedRecord = medicalRecordService.updateMedicalRecord(id, medicalRecordDTO);
            return ResponseEntity.ok(updatedRecord);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error updating medical record: " + e.getMessage()));
        }
    }

    /**
     * Delete a medical record
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('4') or hasAuthority('5')") // Only Admin and Manager
    public ResponseEntity<?> deleteMedicalRecord(@PathVariable Long id) {
        try {
            medicalRecordService.deleteMedicalRecord(id);
            return ResponseEntity.ok(Map.of("message", "Medical record deleted successfully"));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error deleting medical record: " + e.getMessage()));
        }
    }

    /**
     * Update the status of a medical record
     */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('2') or hasAuthority('3') or hasAuthority('4') or hasAuthority('5')") // Doctor, Staff,
                                                                                                      // Admin, Manager
    public ResponseEntity<?> updateMedicalRecordStatus(@PathVariable Long id,
            @RequestBody Map<String, String> statusRequest) {
        String status = statusRequest.get("status");
        try {
            if (status == null || status.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Status is required"));
            }

            MedicalRecordDTO updatedRecord = medicalRecordService.updateMedicalRecordStatus(id, status);
            return ResponseEntity.ok(updatedRecord);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error updating medical record status: " + e.getMessage()));
        }
    }

    /**
     * Get medical records for a patient
     */
    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAuthority('1') or hasAuthority('2') or hasAuthority('3') or hasAuthority('4') or hasAuthority('5')")
    public ResponseEntity<?> getRecordsByPatient(
            @PathVariable Long patientId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        try {
            Map<String, Object> response = medicalRecordService.getMedicalRecordsByPatient(patientId, page, size);
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error retrieving medical records: " + e.getMessage()));
        }
    }

    /**
     * Get medical records for a patient within a date range
     */
    @GetMapping("/patient/{patientId}/date-range")
    @PreAuthorize("hasAuthority('1') or hasAuthority('2') or hasAuthority('3') or hasAuthority('4') or hasAuthority('5')")
    public ResponseEntity<?> getRecordsByPatientAndDateRange(
            @PathVariable Long patientId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        try {
            List<MedicalRecordDTO> records = medicalRecordService.getMedicalRecordsByPatientAndDateRange(patientId,
                    startDate, endDate);
            return ResponseEntity.ok(Map.of("records", records, "count", records.size()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error retrieving medical records: " + e.getMessage()));
        }
    }

    /**
     * Get medical records for a doctor
     */
    @GetMapping("/doctor/{doctorId}")
    @PreAuthorize("hasAuthority('2') or hasAuthority('3') or hasAuthority('4') or hasAuthority('5')") // Doctor, Staff,
                                                                                                      // Admin, Manager
    public ResponseEntity<?> getRecordsByDoctor(
            @PathVariable Long doctorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        try {
            Map<String, Object> response = medicalRecordService.getMedicalRecordsByDoctor(doctorId, page, size);
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error retrieving medical records: " + e.getMessage()));
        }
    }

    /**
     * Get medical records for a doctor on a specific date
     */
    @GetMapping("/doctor/{doctorId}/date")
    @PreAuthorize("hasAuthority('2') or hasAuthority('3') or hasAuthority('4') or hasAuthority('5')") // Doctor, Staff,
                                                                                                      // Admin, Manager
    public ResponseEntity<?> getRecordsByDoctorAndDate(
            @PathVariable Long doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        try {
            List<MedicalRecordDTO> records = medicalRecordService.getMedicalRecordsByDoctorAndDate(doctorId, date);
            return ResponseEntity.ok(Map.of("records", records, "count", records.size()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error retrieving medical records: " + e.getMessage()));
        }
    }

    /**
     * Get statistics about medical records
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasAuthority('3') or hasAuthority('4') or hasAuthority('5')") // Staff, Admin, Manager
    public ResponseEntity<?> getStatistics() {
        try {
            Map<String, Object> stats = new HashMap<>();
            stats.put("inTreatment", medicalRecordService.countMedicalRecordsByStatus("Đang điều trị"));
            stats.put("completed", medicalRecordService.countMedicalRecordsByStatus("Hoàn thành"));

            // Calculate total
            long total = (long) stats.get("inTreatment") + (long) stats.get("completed");
            stats.put("total", total);

            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error retrieving statistics: " + e.getMessage()));
        }
    }
}