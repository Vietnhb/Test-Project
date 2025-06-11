package com.fpt.hivtreatment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentDTO {
    private Long id;
    private Long patientId;
    private String patientName;
    private String appointmentType;
    private String status;
    private String symptoms;
    private String notes;
    private Boolean isAnonymous;
    private String timeSlot;
    private LocalDate date;
}