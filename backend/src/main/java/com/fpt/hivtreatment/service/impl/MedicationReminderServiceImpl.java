package com.fpt.hivtreatment.service.impl;

import com.fpt.hivtreatment.model.entity.MedicationReminder;
import com.fpt.hivtreatment.model.entity.PrescriptionItem;
import com.fpt.hivtreatment.repository.MedicationReminderRepository;
import com.fpt.hivtreatment.repository.PrescriptionItemRepository;
import com.fpt.hivtreatment.service.MedicationReminderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MedicationReminderServiceImpl implements MedicationReminderService {

    private final MedicationReminderRepository medicationReminderRepository;
    private final PrescriptionItemRepository prescriptionItemRepository;

    @Override
    public MedicationReminder createMedicationReminder(MedicationReminder medicationReminder) {
        log.info("Creating medication reminder for patient ID: {}, prescription item ID: {}",
                medicationReminder.getPatientId(), medicationReminder.getPrescriptionItemId());

        MedicationReminder savedReminder = medicationReminderRepository.save(medicationReminder);
        log.info("Created medication reminder with ID: {}", savedReminder.getId());

        return savedReminder;
    }

    @Override
    public List<MedicationReminder> createMedicationReminders(List<MedicationReminder> medicationReminders) {
        log.info("Creating {} medication reminders", medicationReminders.size());

        List<MedicationReminder> savedReminders = new ArrayList<>();
        for (MedicationReminder reminder : medicationReminders) {
            savedReminders.add(createMedicationReminder(reminder));
        }

        return savedReminders;
    }

    @Override
    public List<MedicationReminder> createRemindersFromPrescriptionItems(List<Long> prescriptionItemIds) {
        log.info("Creating medication reminders from {} prescription items", prescriptionItemIds.size());

        List<MedicationReminder> reminders = new ArrayList<>();

        for (Long prescriptionItemId : prescriptionItemIds) {
            PrescriptionItem item = prescriptionItemRepository.findById(prescriptionItemId)
                    .orElseThrow(() -> new RuntimeException("Prescription item not found: " + prescriptionItemId)); // Tạo
                                                                                                                    // reminder
                                                                                                                    // cho
                                                                                                                    // từng
                                                                                                                    // thời
                                                                                                                    // điểm
                                                                                                                    // trong
                                                                                                                    // ngày
                                                                                                                    // nếu
                                                                                                                    // có
                                                                                                                    // liều
            if (item.getMorningDose() != null && item.getMorningDose() > 0) {
                reminders.add(createReminderForTimeSlot(item, MedicationReminder.ReminderType.MORNING,
                        LocalTime.of(8, 0), item.getMorningDose()));
            }
            if (item.getNoonDose() != null && item.getNoonDose() > 0) {
                reminders.add(createReminderForTimeSlot(item, MedicationReminder.ReminderType.NOON, LocalTime.of(12, 0),
                        item.getNoonDose()));
            }
            if (item.getAfternoonDose() != null && item.getAfternoonDose() > 0) {
                reminders.add(createReminderForTimeSlot(item, MedicationReminder.ReminderType.AFTERNOON,
                        LocalTime.of(16, 0), item.getAfternoonDose()));
            }
            if (item.getEveningDose() != null && item.getEveningDose() > 0) {
                reminders.add(createReminderForTimeSlot(item, MedicationReminder.ReminderType.EVENING,
                        LocalTime.of(20, 0), item.getEveningDose()));
            }
        }

        return createMedicationReminders(reminders);
    }

    private MedicationReminder createReminderForTimeSlot(PrescriptionItem item,
            MedicationReminder.ReminderType reminderType, LocalTime reminderTime, Integer dose) {
        MedicationReminder reminder = new MedicationReminder();
        reminder.setPatientId(item.getPrescription().getPatientId());
        reminder.setPrescriptionItemId(item.getId());
        reminder.setReminderType(reminderType);
        reminder.setReminderTime(reminderTime);
        reminder.setDoseAmount(dose);
        reminder.setIsActive(true);
        reminder.setCreatedAt(LocalDateTime.now());

        return reminder;
    }

    @Override
    public MedicationReminder updateMedicationReminder(Long id, MedicationReminder medicationReminder) {
        log.info("Updating medication reminder with ID: {}", id);

        MedicationReminder existingReminder = findById(id);

        existingReminder.setReminderTime(medicationReminder.getReminderTime());
        existingReminder.setReminderType(medicationReminder.getReminderType());
        existingReminder.setDoseAmount(medicationReminder.getDoseAmount());
        existingReminder.setIsActive(medicationReminder.getIsActive());

        return medicationReminderRepository.save(existingReminder);
    }

    @Override
    public void deleteMedicationReminder(Long id) {
        log.info("Deleting medication reminder with ID: {}", id);
        medicationReminderRepository.deleteById(id);
    }

    @Override
    public MedicationReminder findById(Long id) {
        return medicationReminderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Medication reminder not found with ID: " + id));
    }

    @Override
    public List<MedicationReminder> findByPatientId(Long patientId) {
        return medicationReminderRepository.findByPatientId(patientId);
    }

    @Override
    public List<MedicationReminder> findByPrescriptionItemId(Long prescriptionItemId) {
        return medicationReminderRepository.findByPrescriptionItemId(prescriptionItemId);
    }

    @Override
    public List<MedicationReminder> findByReminderTime(LocalDateTime reminderTime) {
        // This method needs to be implemented in repository
        return medicationReminderRepository.findByPrescriptionItemId(null); // Placeholder
    }

    @Override
    public List<MedicationReminder> findByTimeSlot(String timeSlot) {
        // Convert string to enum and search by type
        return medicationReminderRepository.findByPrescriptionItemId(null); // Placeholder
    }

    @Override
    public List<MedicationReminder> findActiveByPatientId(Long patientId) {
        return medicationReminderRepository.findByPatientId(patientId);
    }

    @Override
    public MedicationReminder updateStatus(Long id, String status) {
        log.info("Updating status of medication reminder ID: {} to: {}", id, status);

        MedicationReminder reminder = findById(id);
        // Convert status to boolean for isActive field
        reminder.setIsActive("active".equals(status));

        return medicationReminderRepository.save(reminder);
    }

    @Override
    public List<MedicationReminder> findRemindersToSend(LocalDateTime startTime, LocalDateTime endTime) {
        // This method needs to be implemented in repository
        return medicationReminderRepository.findByPatientId(null); // Placeholder
    }

    @Override
    public MedicationReminder markAsSent(Long id) {
        log.info("Marking medication reminder ID: {} as sent", id);

        MedicationReminder reminder = findById(id);
        reminder.setIsActive(false); // Mark as inactive when sent

        return medicationReminderRepository.save(reminder);
    }

    @Override
    public MedicationReminder toggleReminder(Long id, boolean active) {
        log.info("Toggling medication reminder ID: {} to active: {}", id, active);

        MedicationReminder reminder = findById(id);
        reminder.setIsActive(active);

        MedicationReminder updatedReminder = medicationReminderRepository.save(reminder);
        log.info("Successfully toggled medication reminder ID: {} to active: {}", id, active);

        return updatedReminder;
    }
}
