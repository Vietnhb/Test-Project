package com.fpt.hivtreatment.payload.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentResponse {
    private Long id;
    private Long patientId;
    private String patientName;
    private Long doctorId;
    private String doctorName;
    private Long appointmentSlotId;
    private String appointmentType;
    private String status;
    private Boolean isAnonymous;
    private String symptoms;
    private String notes;
    private String cancellationReason;
    private Date createdAt;
    private String appointmentDate;
    private String startTime;
    private String endTime;
}