package com.fpt.hivtreatment.service.impl;

import com.fpt.hivtreatment.model.entity.Prescription;
import com.fpt.hivtreatment.model.entity.PrescriptionItem;
import com.fpt.hivtreatment.model.entity.MedicationReminder;
import com.fpt.hivtreatment.repository.PrescriptionRepository;
import com.fpt.hivtreatment.service.PrescriptionService;
import com.fpt.hivtreatment.service.PrescriptionItemService;
import com.fpt.hivtreatment.service.MedicationReminderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PrescriptionServiceImplNew implements PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;
    private final PrescriptionItemService prescriptionItemService;
    private final MedicationReminderService medicationReminderService;    @Override
    public Prescription createPrescription(Prescription prescription) {
        log.info("Creating prescription for patient ID: {}", prescription.getPatientId());

        // Set treatment start date if not provided
        if (prescription.getTreatmentStartDate() == null) {
            prescription.setTreatmentStartDate(LocalDate.now());
        }

        // Set created timestamp
        if (prescription.getCreatedAt() == null) {
            prescription.setCreatedAt(LocalDateTime.now());
        }

        Prescription savedPrescription = prescriptionRepository.save(prescription);
        log.info("Created prescription with ID: {}", savedPrescription.getId());

        return savedPrescription;
    }

    @Override
    public Prescription createFullPrescription(Prescription prescription, List<PrescriptionItem> prescriptionItems) {
        log.info("Creating full prescription for patient ID: {} with {} items", 
                prescription.getPatientId(), prescriptionItems.size());

        // Create prescription header
        Prescription savedPrescription = createPrescription(prescription);        // Set prescription ID for all items
        for (PrescriptionItem item : prescriptionItems) {
            item.setPrescriptionId(savedPrescription.getId());
            // Note: PrescriptionItem doesn't have patientId field - it gets patient through prescription relationship
        }

        // Create prescription items
        List<PrescriptionItem> savedItems = prescriptionItemService.createPrescriptionItems(prescriptionItems);

        // Generate medication reminders automatically
        List<Long> itemIds = savedItems.stream()
                .map(PrescriptionItem::getId)
                .collect(Collectors.toList());
        List<MedicationReminder> reminders = medicationReminderService.createRemindersFromPrescriptionItems(itemIds);

        log.info("Created full prescription with ID: {}, {} items, {} reminders", 
                savedPrescription.getId(), savedItems.size(), reminders.size());

        return savedPrescription;
    }

    @Override
    public Prescription updatePrescription(Long id, Prescription prescription) {
        log.info("Updating prescription with ID: {}", id);

        Prescription existingPrescription = findById(id);

        // Update fields
        if (prescription.getProtocolId() != null) {
            existingPrescription.setProtocolId(prescription.getProtocolId());
        }        if (prescription.getDoctorId() != null) {
            existingPrescription.setDoctorId(prescription.getDoctorId());
        }
        if (prescription.getTreatmentStartDate() != null) {
            existingPrescription.setTreatmentStartDate(prescription.getTreatmentStartDate());
        }
        if (prescription.getTreatmentEndDate() != null) {
            existingPrescription.setTreatmentEndDate(prescription.getTreatmentEndDate());
        }
        if (prescription.getDoctorNotes() != null) {
            existingPrescription.setDoctorNotes(prescription.getDoctorNotes());
        }        if (prescription.getStatus() != null) {
            existingPrescription.setStatus(prescription.getStatus());
        }

        // No updatedAt field in current entity, only createdAt

        Prescription updatedPrescription = prescriptionRepository.save(existingPrescription);
        log.info("Updated prescription with ID: {}", updatedPrescription.getId());

        return updatedPrescription;
    }

    @Override
    public void deletePrescription(Long id) {
        log.info("Deleting prescription with ID: {}", id);

        if (!prescriptionRepository.existsById(id)) {
            throw new RuntimeException("Prescription not found with ID: " + id);
        }

        prescriptionRepository.deleteById(id);
        log.info("Deleted prescription with ID: {}", id);
    }

    @Override
    public Prescription findById(Long id) {
        return prescriptionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Prescription not found with ID: " + id));
    }    @Override
    public List<Prescription> findByPatientId(Long patientId) {
        return prescriptionRepository.findByPatientIdOrderByCreatedAtDesc(patientId);
    }

    @Override
    public List<Prescription> findByMedicalRecordId(Long medicalRecordId) {
        return prescriptionRepository.findByMedicalRecordIdOrderByCreatedAtDesc(medicalRecordId);
    }

    @Override
    public List<Prescription> findByMedicalRecordIdWithDetails(Long medicalRecordId) {
        return prescriptionRepository.findByMedicalRecordIdWithDetails(medicalRecordId);
    }

    @Override
    public List<Prescription> findByPatientIdWithDetails(Long patientId) {
        return prescriptionRepository.findByPatientIdWithDetails(patientId);
    }

    @Override
    public List<Prescription> findActivePrescriptionsByPatientId(Long patientId) {
        return prescriptionRepository.findActivePrescriptionsByPatientId(patientId);
    }    @Override
    public List<Prescription> findByDateRange(LocalDate startDate, LocalDate endDate) {
        return prescriptionRepository.findByCreatedAtBetween(startDate, endDate);
    }

    @Override
    public List<Prescription> findActivePrescriptionsBetween(LocalDate startDate, LocalDate endDate) {
        return prescriptionRepository.findActivePrescriptionsBetween(startDate, endDate);
    }

    @Override
    public List<Object[]> getPrescriptionStatsByMedication(LocalDate startDate, LocalDate endDate) {
        return prescriptionRepository.countPrescriptionsByMedication(startDate, endDate);
    }    @Override
    public PrescriptionItem addPrescriptionItem(Long prescriptionId, PrescriptionItem prescriptionItem) {
        log.info("Adding prescription item to prescription ID: {}", prescriptionId);        
        // Validate that prescription exists
        findById(prescriptionId);
        prescriptionItem.setPrescriptionId(prescriptionId);
        // Note: PrescriptionItem doesn't have patientId field - it gets patient through prescription relationship

        PrescriptionItem savedItem = prescriptionItemService.createPrescriptionItem(prescriptionItem);

        // Generate medication reminders for the new item
        List<Long> itemIds = List.of(savedItem.getId());
        medicationReminderService.createRemindersFromPrescriptionItems(itemIds);

        return savedItem;
    }

    @Override
    public void removePrescriptionItem(Long prescriptionId, Long prescriptionItemId) {
        log.info("Removing prescription item ID: {} from prescription ID: {}", prescriptionItemId, prescriptionId);

        prescriptionItemService.deletePrescriptionItem(prescriptionItemId);
    }

    @Override
    public List<MedicationReminder> generateMedicationReminders(Long prescriptionId) {
        log.info("Generating medication reminders for prescription ID: {}", prescriptionId);

        List<PrescriptionItem> items = prescriptionItemService.findByPrescriptionId(prescriptionId);
        List<Long> itemIds = items.stream()
                .map(PrescriptionItem::getId)
                .collect(Collectors.toList());

        return medicationReminderService.createRemindersFromPrescriptionItems(itemIds);
    }

    @Override
    public Prescription updatePrescriptionStatus(Long id, String status) {
        log.info("Updating status of prescription ID: {} to: {}", id, status);        Prescription prescription = findById(id);
        prescription.setStatus(status);
        // Note: Prescription entity only has createdAt field, no updatedAt field

        return prescriptionRepository.save(prescription);
    }

    @Override
    public Prescription cancelPrescription(Long id) {
        log.info("Cancelling prescription ID: {}", id);

        return updatePrescriptionStatus(id, "cancelled");
    }

    @Override
    public Prescription completePrescription(Long id) {
        log.info("Completing prescription ID: {}", id);

        return updatePrescriptionStatus(id, "completed");
    }
}
