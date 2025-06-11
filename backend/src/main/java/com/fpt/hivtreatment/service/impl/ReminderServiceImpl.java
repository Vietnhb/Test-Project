package com.fpt.hivtreatment.service.impl;

import com.fpt.hivtreatment.model.entity.Reminder;
import com.fpt.hivtreatment.repository.ReminderRepository;
import com.fpt.hivtreatment.service.ReminderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ReminderServiceImpl implements ReminderService {

    private final ReminderRepository reminderRepository;

    @Override
    public Reminder createReminder(Reminder reminder) {
        log.info("Creating reminder for patient ID: {}, reminderType: {}",
                reminder.getPatientId(), reminder.getReminderType());

        if (reminder.getCreatedAt() == null) {
            reminder.setCreatedAt(LocalDateTime.now());
        }

        Reminder savedReminder = reminderRepository.save(reminder);
        log.info("Created reminder with ID: {}", savedReminder.getId());

        return savedReminder;
    }

    @Override
    public List<Reminder> createReminders(List<Reminder> reminders) {
        log.info("Creating {} reminders", reminders.size());

        List<Reminder> savedReminders = new ArrayList<>();
        for (Reminder reminder : reminders) {
            savedReminders.add(createReminder(reminder));
        }

        return savedReminders;
    }

    @Override
    public Reminder updateReminder(Long id, Reminder reminder) {
        log.info("Updating reminder with ID: {}", id);

        Reminder existingReminder = findById(id);

        existingReminder.setTitle(reminder.getTitle());
        existingReminder.setMessage(reminder.getMessage());
        existingReminder.setReminderTime(reminder.getReminderTime());
        existingReminder.setReminderType(reminder.getReminderType());
        existingReminder.setIsActive(reminder.getIsActive());
        existingReminder.setUpdatedAt(LocalDateTime.now());

        return reminderRepository.save(existingReminder);
    }

    @Override
    public void deleteReminder(Long id) {
        log.info("Deleting reminder with ID: {}", id);
        reminderRepository.deleteById(id);
    }

    @Override
    public Reminder findById(Long id) {
        return reminderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reminder not found with ID: " + id));
    }

    @Override
    public List<Reminder> findByPatientId(Long patientId) {
        return reminderRepository.findByPatientId(patientId);
    }

    @Override
    public List<Reminder> findByType(String type) {
        return reminderRepository.findByReminderType(type);
    }

    @Override
    public List<Reminder> findByReminderTime(LocalDateTime reminderTime) {
        // Since there's no direct repository method, we'll use a custom query approach
        // For now, return all reminders and filter in service layer or implement custom
        // query
        return reminderRepository.findAll().stream()
                .filter(reminder -> reminder.getReminderTime() != null &&
                        reminder.getReminderTime().equals(reminderTime.toLocalTime()))
                .toList();
    }

    @Override
    public List<Reminder> findActiveByPatientId(Long patientId) {
        return reminderRepository.findByPatientIdAndIsActive(patientId, true);
    }

    @Override
    public List<Reminder> findByPatientIdAndType(Long patientId, String type) {
        // Use existing repository method with correct field name
        return reminderRepository.findByPatientId(patientId).stream()
                .filter(reminder -> type.equals(reminder.getReminderType()))
                .toList();
    }

    @Override
    public Reminder updateStatus(Long id, String status) {
        log.info("Updating status of reminder ID: {} to: {}", id, status);

        Reminder reminder = findById(id);
        // Convert string status to boolean active state
        reminder.setIsActive(!"inactive".equalsIgnoreCase(status) && !"disabled".equalsIgnoreCase(status));
        reminder.setUpdatedAt(LocalDateTime.now());

        return reminderRepository.save(reminder);
    }

    @Override
    public List<Reminder> findRemindersToSend(LocalDateTime startTime, LocalDateTime endTime) {
        // Since the repository doesn't have this method, we'll use existing methods
        // to find active reminders and filter by date range
        return reminderRepository.findAll().stream()
                .filter(reminder -> reminder.getIsActive() != null && reminder.getIsActive())
                .filter(reminder -> {
                    if (reminder.getReminderDate() != null) {
                        return !reminder.getReminderDate().isBefore(startTime.toLocalDate())
                                && !reminder.getReminderDate().isAfter(endTime.toLocalDate());
                    }
                    return false;
                })
                .toList();
    }

    @Override
    public Reminder markAsSent(Long id) {
        log.info("Marking reminder ID: {} as sent", id);

        Reminder reminder = findById(id);
        // Since there's no status field, we can mark as inactive or add a custom field
        // For now, we'll mark as inactive to indicate it has been sent
        reminder.setIsActive(false);
        reminder.setUpdatedAt(LocalDateTime.now());

        return reminderRepository.save(reminder);
    }

    @Override
    public List<Reminder> findByStatus(String status) {
        // Convert string status to boolean active state for filtering
        Boolean isActive = !"inactive".equalsIgnoreCase(status) && !"disabled".equalsIgnoreCase(status);
        return reminderRepository.findAll().stream()
                .filter(reminder -> isActive.equals(reminder.getIsActive()))
                .toList();
    }

    @Override
    public List<Reminder> findActiveRemindersByDate(Long patientId, java.time.LocalDate date) {
        log.info("Finding active reminders for patient ID: {} on date: {}", patientId, date);
        
        return reminderRepository.findAll().stream()
                .filter(reminder -> reminder.getPatientId().equals(patientId))
                .filter(reminder -> reminder.getIsActive() != null && reminder.getIsActive())
                .filter(reminder -> {
                    // Check if reminder is applicable for the given date
                    if (reminder.getReminderDate() != null) {
                        return reminder.getReminderDate().equals(date);
                    }
                    // If no specific date, check if it falls within start/end date range
                    if (reminder.getStartDate() != null && reminder.getEndDate() != null) {
                        return !date.isBefore(reminder.getStartDate()) && !date.isAfter(reminder.getEndDate());
                    }
                    // If only start date, check if date is after start
                    if (reminder.getStartDate() != null) {
                        return !date.isBefore(reminder.getStartDate());
                    }
                    return true; // Default case
                })
                .toList();
    }
}
