package com.fpt.hivtreatment.controller;

import com.fpt.hivtreatment.model.entity.MedicationReminder;
import com.fpt.hivtreatment.model.entity.Reminder;
import com.fpt.hivtreatment.payload.request.ReminderRequest;
import com.fpt.hivtreatment.payload.response.MedicationReminderResponse;
import com.fpt.hivtreatment.payload.response.MessageResponse;
import com.fpt.hivtreatment.payload.response.ReminderResponse;
import com.fpt.hivtreatment.service.MedicationReminderService;
import com.fpt.hivtreatment.service.ReminderService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reminders")
@RequiredArgsConstructor
@Slf4j
public class ReminderController {

    private final ReminderService reminderService;
    private final MedicationReminderService medicationReminderService;

    /**
     * Get all reminders for a patient
     */
    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyAuthority('1', '2', '5')") // Patient, Doctor, Manager
    public ResponseEntity<?> getPatientReminders(@PathVariable Long patientId) {
        log.info("Fetching reminders for patient ID: {}", patientId);

        try {
            // Get general reminders
            List<Reminder> generalReminders = reminderService.findByPatientId(patientId);
            List<ReminderResponse> generalResponses = generalReminders.stream()
                    .map(this::convertToReminderResponse)
                    .collect(Collectors.toList());

            // Get medication reminders
            List<MedicationReminder> medicationReminders = medicationReminderService.findByPatientId(patientId);
            List<MedicationReminderResponse> medicationResponses = medicationReminders.stream()
                    .map(this::convertToMedicationReminderResponse)
                    .collect(Collectors.toList());

            Map<String, Object> result = new HashMap<>();
            result.put("generalReminders", generalResponses);
            result.put("medicationReminders", medicationResponses);
            result.put("totalCount", generalResponses.size() + medicationResponses.size());

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error fetching reminders for patient", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error fetching reminders: " + e.getMessage()));
        }
    }

    /**
     * Get active reminders for today for a patient
     */
    @GetMapping("/patient/{patientId}/today")
    @PreAuthorize("hasAnyAuthority('1', '2', '5')") // Patient, Doctor, Manager
    public ResponseEntity<?> getTodayReminders(@PathVariable Long patientId) {
        log.info("Fetching today's reminders for patient ID: {}", patientId);
        LocalDate today = LocalDate.now();

        try {
            // Get general reminders for today
            List<Reminder> generalReminders = reminderService.findActiveRemindersByDate(patientId, today);
            List<ReminderResponse> generalResponses = generalReminders.stream()
                    .map(this::convertToReminderResponse)
                    .collect(Collectors.toList());

            // Get medication reminders (all active ones)
            List<MedicationReminder> medicationReminders = medicationReminderService.findActiveByPatientId(patientId);
            List<MedicationReminderResponse> medicationResponses = medicationReminders.stream()
                    .map(this::convertToMedicationReminderResponse)
                    .collect(Collectors.toList());

            Map<String, Object> result = new HashMap<>();
            result.put("date", today.format(DateTimeFormatter.ISO_LOCAL_DATE));
            result.put("generalReminders", generalResponses);
            result.put("medicationReminders", medicationResponses);
            result.put("totalCount", generalResponses.size() + medicationResponses.size());

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error fetching today's reminders for patient", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error fetching today's reminders: " + e.getMessage()));
        }
    }

    /**
     * Create a new general reminder
     */
    @PostMapping
    @PreAuthorize("hasAnyAuthority('1', '2', '5')") // Patient, Doctor, Manager
    public ResponseEntity<?> createReminder(@Valid @RequestBody ReminderRequest request) {
        log.info("Creating reminder for patient ID: {}", request.getPatientId());

        try {
            Reminder reminder = convertToReminderEntity(request);
            Reminder savedReminder = reminderService.createReminder(reminder);

            ReminderResponse response = convertToReminderResponse(savedReminder);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("Error creating reminder", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error creating reminder: " + e.getMessage()));
        }
    }

    /**
     * Update an existing reminder
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('1', '2', '5')") // Patient, Doctor, Manager
    public ResponseEntity<?> updateReminder(@PathVariable Long id, @Valid @RequestBody ReminderRequest request) {
        log.info("Updating reminder ID: {}", id);

        try {
            Reminder reminder = convertToReminderEntity(request);
            Reminder updatedReminder = reminderService.updateReminder(id, reminder);

            ReminderResponse response = convertToReminderResponse(updatedReminder);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error updating reminder", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error updating reminder: " + e.getMessage()));
        }
    }

    /**
     * Delete a reminder
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('1', '2', '5')") // Patient, Doctor, Manager
    public ResponseEntity<?> deleteReminder(@PathVariable Long id) {
        log.info("Deleting reminder ID: {}", id);

        try {
            reminderService.deleteReminder(id);
            return ResponseEntity.ok(new MessageResponse("Reminder deleted successfully"));
        } catch (Exception e) {
            log.error("Error deleting reminder", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error deleting reminder: " + e.getMessage()));
        }
    }

    /**
     * Activate/deactivate medication reminder
     */
    @PutMapping("/medication/{id}/toggle")
    @PreAuthorize("hasAnyAuthority('1', '2', '5')") // Patient, Doctor, Manager
    public ResponseEntity<?> toggleMedicationReminder(@PathVariable Long id, @RequestParam boolean active) {
        log.info("Toggling medication reminder ID: {} to active: {}", id, active);

        try {
            MedicationReminder updatedReminder = medicationReminderService.toggleReminder(id, active);
            MedicationReminderResponse response = convertToMedicationReminderResponse(updatedReminder);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error toggling medication reminder", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error toggling medication reminder: " + e.getMessage()));
        }
    }

    // Helper methods for entity conversion
    private Reminder convertToReminderEntity(ReminderRequest request) {
        Reminder reminder = new Reminder();
        reminder.setPatientId(request.getPatientId());
        reminder.setPrescriptionId(request.getPrescriptionId());
        reminder.setMedicalRecordId(request.getMedicalRecordId());
        reminder.setReminderType(request.getReminderType());
        reminder.setTitle(request.getTitle());
        reminder.setMessage(request.getMessage());
        reminder.setStartDate(request.getStartDate());
        reminder.setEndDate(request.getEndDate());
        reminder.setReminderTime(request.getReminderTime());
        reminder.setReminderDate(request.getReminderDate());
        reminder.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);

        return reminder;
    }

    private ReminderResponse convertToReminderResponse(Reminder reminder) {
        ReminderResponse response = new ReminderResponse();
        response.setId(reminder.getId());
        response.setPatientId(reminder.getPatientId());
        response.setPrescriptionId(reminder.getPrescriptionId());
        response.setMedicalRecordId(reminder.getMedicalRecordId());
        response.setReminderType(reminder.getReminderType());
        response.setTitle(reminder.getTitle());
        response.setMessage(reminder.getMessage());
        response.setStartDate(reminder.getStartDate());
        response.setEndDate(reminder.getEndDate());
        response.setReminderTime(reminder.getReminderTime());
        response.setReminderDate(reminder.getReminderDate());
        response.setIsActive(reminder.getIsActive());
        response.setCreatedAt(reminder.getCreatedAt());
        response.setUpdatedAt(reminder.getUpdatedAt());

        // Add patient name if available
        if (reminder.getPatient() != null) {
            response.setPatientName(reminder.getPatient().getFullName());
        }

        return response;
    }

    private MedicationReminderResponse convertToMedicationReminderResponse(MedicationReminder reminder) {
        MedicationReminderResponse response = new MedicationReminderResponse();
        response.setId(reminder.getId());
        response.setPatientId(reminder.getPatientId());
        response.setPrescriptionItemId(reminder.getPrescriptionItemId());
        response.setReminderType(reminder.getReminderType().name());
        response.setReminderTime(reminder.getReminderTime());
        response.setDoseAmount(reminder.getDoseAmount());
        response.setIsActive(reminder.getIsActive());
        response.setCreatedAt(reminder.getCreatedAt());

        // Add medication details if available
        if (reminder.getPrescriptionItem() != null) {
            if (reminder.getPrescriptionItem().getMedication() != null) {
                response.setMedicationName(reminder.getPrescriptionItem().getMedication().getName());
                response.setMedicationDosageForm(reminder.getPrescriptionItem().getMedication().getDosageForm());
            }

            // Set prescription ID directly from the prescription item
            response.setPrescriptionId(reminder.getPrescriptionItem().getPrescriptionId());
        }

        return response;
    }
}