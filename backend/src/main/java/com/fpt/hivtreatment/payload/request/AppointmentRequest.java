package com.fpt.hivtreatment.payload.request;

import lombok.Data;


@Data
public class AppointmentRequest {
    private Long patientId;
    private Long doctorId;
    private Long appointmentSlotId;
    private String appointmentType;
    private Boolean isAnonymous = false;
    private String symptoms;
    private String notes;
    private String appointmentDate;
}