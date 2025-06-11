package com.fpt.hivtreatment.payload.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class ReminderRequest {

    @NotNull(message = "Patient ID is required")
    private Long patientId;

    private Long prescriptionId;

    private Long medicalRecordId;

    @NotBlank(message = "Reminder type is required")
    private String reminderType; // 'MEDICATION', 'APPOINTMENT'

    @NotBlank(message = "Title is required")
    private String title;

    private String message;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    private LocalDate endDate;

    private LocalTime reminderTime;

    private LocalDate reminderDate;

    private Boolean isActive;
}