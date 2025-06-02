package com.fpt.hivtreatment.payload.request;

import lombok.Data;

@Data
public class GenerateAppointmentSlotsRequest {
    private Long doctor_schedule_id;
    private Long doctor_id;
    private String schedule_date;
    private Long work_shift_id;
}