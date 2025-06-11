package com.fpt.hivtreatment.payload.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MedicationReminderResponse {

    private Long id;
    private Long patientId;
    private Long prescriptionItemId;
    private Long prescriptionId;
    private Long medicationId;
    private String reminderType; // morning, noon, afternoon, evening
    private LocalTime reminderTime;
    private Integer doseAmount;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Related entities
    private String medicationName;
    private String medicationDosageForm;
    private String patientName;
}
