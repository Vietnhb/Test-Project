package com.fpt.hivtreatment.payload.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalTime;

@Data
public class MedicationReminderRequest {

    @NotNull(message = "prescriptionItemId is required")
    private Long prescriptionItemId;

    @NotNull(message = "patientId is required")
    private Long patientId;

    @NotNull(message = "reminderType is required")
    private String reminderType; // 'MORNING', 'NOON', 'AFTERNOON', 'EVENING'

    @NotNull(message = "reminderTime is required")
    private LocalTime reminderTime;

    @NotNull(message = "doseAmount is required")
    @Min(value = 1, message = "doseAmount must be at least 1")
    private Integer doseAmount;

    private Boolean isActive = true;
}