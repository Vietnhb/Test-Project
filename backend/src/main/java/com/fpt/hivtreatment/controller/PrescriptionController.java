package com.fpt.hivtreatment.controller;

import com.fpt.hivtreatment.model.entity.Prescription;
import com.fpt.hivtreatment.model.entity.PrescriptionItem;
import com.fpt.hivtreatment.model.entity.MedicationReminder;
import com.fpt.hivtreatment.payload.request.PrescriptionRequest;
import com.fpt.hivtreatment.payload.request.PrescriptionItemRequest;
import com.fpt.hivtreatment.payload.response.PrescriptionResponse;
import com.fpt.hivtreatment.payload.response.PrescriptionItemResponse;
import com.fpt.hivtreatment.payload.response.MedicationReminderResponse;
import com.fpt.hivtreatment.payload.response.MessageResponse;
import com.fpt.hivtreatment.service.PrescriptionService;
import com.fpt.hivtreatment.service.PrescriptionItemService;
import com.fpt.hivtreatment.service.MedicationReminderService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/prescriptions")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class PrescriptionController {

    private final PrescriptionService prescriptionService;
    private final PrescriptionItemService prescriptionItemService;
    private final MedicationReminderService medicationReminderService;

    /**
     * Create a complete prescription with items and automatic reminders
     * This is the main endpoint doctors will use from TreatmentPlanForm.js
     */
    @PostMapping
    @PreAuthorize("hasAuthority('2')") // Doctor role
    public ResponseEntity<?> createPrescription(@Valid @RequestBody PrescriptionRequest request) {
        log.info("Creating prescription for patient ID: {} by doctor ID: {}",
                request.getPatientId(), request.getDoctorId());

        try {
            // Convert request to entities
            Prescription prescription = convertToPrescriptionEntity(request);
            List<PrescriptionItem> prescriptionItems = convertToPrescriptionItemEntities(
                    request.getPrescriptionItems());

            // Create full prescription with items and reminders
            Prescription savedPrescription = prescriptionService.createFullPrescription(prescription,
                    prescriptionItems);

            // Convert to response
            PrescriptionResponse response = convertToPrescriptionResponse(savedPrescription);

            log.info("Successfully created prescription with ID: {}", savedPrescription.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            log.error("Error creating prescription", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error creating prescription: " + e.getMessage()));
        }
    }

    /**
     * Create multiple prescriptions in batch (called from ExaminationForm.js)
     * This endpoint automatically creates reminders for each prescription item
     */
    @PostMapping("/batch")
    @PreAuthorize("hasAuthority('2')") // Doctor role
    public ResponseEntity<?> createPrescriptionsBatch(@Valid @RequestBody List<Map<String, Object>> batchData) {
        log.info("Creating batch prescriptions from ExaminationForm - count: {}", batchData.size());

        try {
            List<PrescriptionResponse> responses = new ArrayList<>();

            // Group by medical record to create prescription headers
            Map<Long, List<Map<String, Object>>> groupedByMedicalRecord = batchData.stream()
                    .collect(Collectors.groupingBy(item -> Long.valueOf(item.get("medicalRecordId").toString())));

            for (Map.Entry<Long, List<Map<String, Object>>> entry : groupedByMedicalRecord.entrySet()) {
                Long medicalRecordId = entry.getKey();
                List<Map<String, Object>> medicationItems = entry.getValue();

                // Get common info from first item
                Map<String, Object> firstItem = medicationItems.get(0);

                // Create prescription header
                Prescription prescription = new Prescription();
                prescription.setMedicalRecordId(medicalRecordId);
                prescription.setPatientId(Long.valueOf(firstItem.get("patientId").toString()));
                prescription.setDoctorId(Long.valueOf(firstItem.get("doctorId").toString()));

                if (firstItem.get("protocolId") != null && !firstItem.get("protocolId").toString().equals("null")) {
                    prescription.setProtocolId(Long.valueOf(firstItem.get("protocolId").toString()));
                }

                prescription.setTreatmentStartDate(LocalDate.parse(firstItem.get("prescriptionDate").toString()));
                prescription.setTreatmentEndDate(
                        LocalDate.parse(firstItem.get("prescriptionDate").toString()).plusDays(30));
                prescription.setStatus(firstItem.get("status").toString());
                prescription.setDoctorNotes("Tạo từ khám bệnh - " + medicationItems.size() + " loại thuốc"); // Create
                                                                                                             // prescription
                                                                                                             // items
                                                                                                             // from
                                                                                                             // medications
                List<PrescriptionItem> prescriptionItems = new ArrayList<>();
                for (Map<String, Object> medItem : medicationItems) {
                    PrescriptionItem item = new PrescriptionItem();
                    item.setMedicationId(Long.valueOf(medItem.get("medicationId").toString()));

                    // Use detailed dosing information if available (from TreatmentPlanForm) // Get
                    // frequency for usage instructions
                    String frequency = medItem.get("frequency") != null ? medItem.get("frequency").toString()
                            : "1 lần/ngày";

                    if (medItem.containsKey("morningDose")) {
                        item.setMorningDose(Integer.valueOf(medItem.get("morningDose").toString()));
                        item.setNoonDose(Integer.valueOf(medItem.get("noonDose").toString()));
                        item.setAfternoonDose(Integer.valueOf(medItem.get("afternoonDose").toString()));
                        item.setEveningDose(Integer.valueOf(medItem.get("eveningDose").toString()));
                    } else {
                        // Fallback: Parse frequency to determine dose distribution
                        item.setMorningDose(1); // Default morning dose
                        item.setNoonDose(0); // No noon dose by default
                        item.setAfternoonDose(0); // No afternoon dose by default
                        item.setEveningDose(0); // No evening dose by default

                        if (frequency.contains("2 lần")) {
                            item.setMorningDose(1);
                            item.setEveningDose(1);
                        } else if (frequency.contains("3 lần")) {
                            item.setMorningDose(1);
                            item.setNoonDose(1);
                            item.setEveningDose(1);
                        } else if (frequency.contains("4 lần")) {
                            item.setMorningDose(1);
                            item.setNoonDose(1);
                            item.setAfternoonDose(1);
                            item.setEveningDose(1);
                        }
                    }

                    String dosage = medItem.get("dosage").toString();
                    String timing = medItem.get("timing").toString();
                    String notes = medItem.get("notes") != null ? medItem.get("notes").toString() : "";

                    item.setUsageInstructions(dosage + " - " + frequency + " - " + timing +
                            (notes.isEmpty() ? "" : " - " + notes));

                    prescriptionItems.add(item);
                }

                // Create full prescription with automatic reminders
                Prescription savedPrescription = prescriptionService.createFullPrescription(prescription,
                        prescriptionItems);

                // Convert to response
                PrescriptionResponse response = convertToPrescriptionResponse(savedPrescription);
                responses.add(response);
            }

            Map<String, Object> result = new HashMap<>();
            result.put("prescriptions", responses);
            result.put("totalCount", responses.size());
            result.put("message",
                    "Successfully created " + responses.size() + " prescriptions with automatic reminders");

            log.info("Successfully created {} prescriptions in batch with automatic reminders", responses.size());
            return ResponseEntity.status(HttpStatus.CREATED).body(result);

        } catch (Exception e) {
            log.error("Error creating batch prescriptions", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error creating batch prescriptions: " + e.getMessage()));
        }
    }

    /**
     * Get prescription by ID with full details
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('1', '2', '5')") // Patient, Doctor, Manager
    public ResponseEntity<?> getPrescriptionById(@PathVariable Long id) {
        log.info("Fetching prescription with ID: {}", id);

        try {
            Prescription prescription = prescriptionService.findById(id);
            PrescriptionResponse response = convertToPrescriptionResponse(prescription);

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.error("Prescription not found with ID: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse("Prescription not found"));
        } catch (Exception e) {
            log.error("Error fetching prescription", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error fetching prescription: " + e.getMessage()));
        }
    }

    /**
     * Get prescriptions by patient ID
     */
    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyAuthority('1', '2', '5')") // Patient, Doctor, Manager
    public ResponseEntity<?> getPrescriptionsByPatient(@PathVariable Long patientId) {
        log.info("Fetching prescriptions for patient ID: {}", patientId);

        try {
            List<Prescription> prescriptions = prescriptionService.findByPatientIdWithDetails(patientId);
            List<PrescriptionResponse> responses = prescriptions.stream()
                    .map(this::convertToPrescriptionResponse)
                    .collect(Collectors.toList());

            Map<String, Object> result = new HashMap<>();
            result.put("prescriptions", responses);
            result.put("totalCount", responses.size());

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error fetching prescriptions for patient", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error fetching prescriptions: " + e.getMessage()));
        }
    }

    /**
     * Get prescriptions by medical record ID
     */
    @GetMapping("/medical-record/{medicalRecordId}")
    @PreAuthorize("hasAnyAuthority('2', '5')") // Doctor, Manager
    public ResponseEntity<?> getPrescriptionsByMedicalRecord(@PathVariable Long medicalRecordId) {
        log.info("Fetching prescriptions for medical record ID: {}", medicalRecordId);

        try {
            List<Prescription> prescriptions = prescriptionService.findByMedicalRecordIdWithDetails(medicalRecordId);
            List<PrescriptionResponse> responses = prescriptions.stream()
                    .map(this::convertToPrescriptionResponse)
                    .collect(Collectors.toList());

            Map<String, Object> result = new HashMap<>();
            result.put("prescriptions", responses);
            result.put("totalCount", responses.size());

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error fetching prescriptions for medical record", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error fetching prescriptions: " + e.getMessage()));
        }
    }

    /**
     * Get active prescriptions for a patient
     */
    @GetMapping("/patient/{patientId}/active")
    @PreAuthorize("hasAnyAuthority('1', '2', '5')") // Patient, Doctor, Manager
    public ResponseEntity<?> getActivePrescriptionsByPatient(@PathVariable Long patientId) {
        log.info("Fetching active prescriptions for patient ID: {}", patientId);

        try {
            List<Prescription> prescriptions = prescriptionService.findActivePrescriptionsByPatientId(patientId);
            List<PrescriptionResponse> responses = prescriptions.stream()
                    .map(this::convertToPrescriptionResponse)
                    .collect(Collectors.toList());

            Map<String, Object> result = new HashMap<>();
            result.put("prescriptions", responses);
            result.put("totalCount", responses.size());

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error fetching active prescriptions for patient", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error fetching active prescriptions: " + e.getMessage()));
        }
    }

    /**
     * Update prescription status
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyAuthority('2', '5')") // Doctor, Manager
    public ResponseEntity<?> updatePrescriptionStatus(@PathVariable Long id, @RequestParam String status) {
        log.info("Updating prescription ID: {} status to: {}", id, status);

        try {
            Prescription updatedPrescription = prescriptionService.updatePrescriptionStatus(id, status);
            PrescriptionResponse response = convertToPrescriptionResponse(updatedPrescription);

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.error("Prescription not found with ID: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse("Prescription not found"));
        } catch (Exception e) {
            log.error("Error updating prescription status", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error updating prescription status: " + e.getMessage()));
        }
    }

    /**
     * Add prescription item to existing prescription
     */
    @PostMapping("/{id}/items")
    @PreAuthorize("hasAnyAuthority('2')") // Doctor only
    public ResponseEntity<?> addPrescriptionItem(@PathVariable Long id,
            @Valid @RequestBody PrescriptionItemRequest request) {
        log.info("Adding prescription item to prescription ID: {}", id);

        try {
            PrescriptionItem item = convertToPrescriptionItemEntity(request);
            PrescriptionItem savedItem = prescriptionService.addPrescriptionItem(id, item);

            PrescriptionItemResponse response = convertToPrescriptionItemResponse(savedItem);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error adding prescription item", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error adding prescription item: " + e.getMessage()));
        }
    }

    /**
     * Get medication reminders for a prescription
     */
    @GetMapping("/{id}/reminders")
    @PreAuthorize("hasAnyAuthority('1', '2', '5')") // Patient, Doctor, Manager
    public ResponseEntity<?> getMedicationReminders(@PathVariable Long id) {
        log.info("Fetching medication reminders for prescription ID: {}", id);

        try {
            // First get all prescription items for this prescription
            List<PrescriptionItem> prescriptionItems = prescriptionItemService.findByPrescriptionId(id);

            // Then get all medication reminders for these prescription items
            List<MedicationReminder> allReminders = new ArrayList<>();
            for (PrescriptionItem item : prescriptionItems) {
                List<MedicationReminder> itemReminders = medicationReminderService
                        .findByPrescriptionItemId(item.getId());
                allReminders.addAll(itemReminders);
            }

            List<MedicationReminderResponse> responses = allReminders.stream()
                    .map(this::convertToMedicationReminderResponse)
                    .collect(Collectors.toList());

            Map<String, Object> result = new HashMap<>();
            result.put("reminders", responses);
            result.put("totalCount", responses.size());

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error fetching medication reminders", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error fetching reminders: " + e.getMessage()));
        }
    }

    // Helper methods for entity conversion
    private Prescription convertToPrescriptionEntity(PrescriptionRequest request) {
        Prescription prescription = new Prescription();
        prescription.setPatientId(request.getPatientId());
        prescription.setMedicalRecordId(request.getMedicalRecordId());
        prescription.setDoctorId(request.getDoctorId());
        prescription.setProtocolId(request.getProtocolId());
        prescription.setTreatmentStartDate(request.getStartDate());
        prescription.setTreatmentEndDate(request.getEndDate());
        prescription.setDoctorNotes(request.getDoctorNotes());
        prescription.setStatus(request.getStatus());

        return prescription;
    }

    private List<PrescriptionItem> convertToPrescriptionItemEntities(List<PrescriptionItemRequest> requests) {
        return requests.stream()
                .map(this::convertToPrescriptionItemEntity)
                .collect(Collectors.toList());
    }

    private PrescriptionItem convertToPrescriptionItemEntity(PrescriptionItemRequest request) {
        PrescriptionItem item = new PrescriptionItem();
        item.setMedicationId(request.getMedicationId());
        item.setMorningDose(request.getMorningDose().intValue());
        item.setNoonDose(request.getNoonDose().intValue());
        item.setAfternoonDose(request.getAfternoonDose().intValue());
        item.setEveningDose(request.getEveningDose().intValue());
        item.setUsageInstructions(request.getInstructions());
        // Note: status field not available in current entity

        return item;
    }

    private PrescriptionResponse convertToPrescriptionResponse(Prescription prescription) {
        return PrescriptionResponse.builder()
                .id(prescription.getId())
                .patientId(prescription.getPatientId())
                .medicalRecordId(prescription.getMedicalRecordId())
                .doctorId(prescription.getDoctorId())
                .protocolId(prescription.getProtocolId())
                .startDate(prescription.getTreatmentStartDate())
                .endDate(prescription.getTreatmentEndDate())
                .doctorNotes(prescription.getDoctorNotes())
                .status(prescription.getStatus())
                .createdAt(prescription.getCreatedAt())
                .build();
    }

    private PrescriptionItemResponse convertToPrescriptionItemResponse(PrescriptionItem item) {
        return PrescriptionItemResponse.builder()
                .id(item.getId())
                .prescriptionId(item.getPrescriptionId())
                .medicationId(item.getMedicationId())
                .morningDose(item.getMorningDose().doubleValue())
                .noonDose(item.getNoonDose().doubleValue())
                .afternoonDose(item.getAfternoonDose().doubleValue())
                .eveningDose(item.getEveningDose().doubleValue())
                .dailyTotal(item.getDailyTotal().doubleValue())
                .instructions(item.getUsageInstructions())
                .build();
    }    private MedicationReminderResponse convertToMedicationReminderResponse(MedicationReminder reminder) {
        MedicationReminderResponse response = new MedicationReminderResponse();
        response.setId(reminder.getId());
        response.setPatientId(reminder.getPatientId());
        response.setPrescriptionItemId(reminder.getPrescriptionItemId());
        response.setReminderType(reminder.getReminderType().name().toLowerCase());
        response.setReminderTime(reminder.getReminderTime()); // LocalTime matches the field type
        response.setDoseAmount(reminder.getDoseAmount());
        response.setIsActive(reminder.getIsActive());
        response.setCreatedAt(reminder.getCreatedAt());

        // Add related data if available
        if (reminder.getPrescriptionItem() != null) {
            response.setPrescriptionId(reminder.getPrescriptionItem().getPrescriptionId());
            if (reminder.getPrescriptionItem().getMedication() != null) {
                response.setMedicationName(reminder.getPrescriptionItem().getMedication().getName());
                response.setMedicationDosageForm(reminder.getPrescriptionItem().getMedication().getDosageForm());
            }
        }

        return response;
    }
}
