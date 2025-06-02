package com.fpt.hivtreatment.payload.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorScheduleResponse {
    private Long id;
    private Long doctorId;
    private String doctorName;
    private LocalDate scheduleDate;
    private LocalTime startTime;
    private LocalTime endTime;
}