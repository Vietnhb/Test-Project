package com.fpt.hivtreatment.payload.response;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
public class ReminderResponse {
    private Long id;
    private Long patientId;
    private String patientName;
    private Long prescriptionId;
    private Long medicalRecordId;
    private String reminderType;
    private String title;
    private String message;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalTime reminderTime;
    private LocalDate reminderDate;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}