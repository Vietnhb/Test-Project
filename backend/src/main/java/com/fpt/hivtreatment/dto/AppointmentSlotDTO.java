package com.fpt.hivtreatment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppointmentSlotDTO {
    private Long id;
    private Long doctorScheduleId;
    private Long timeSlotId;
    private Long doctorId;
    private Boolean isAvailable;
    private Date createdAt;

    // Thêm trường thông tin bổ sung cho frontend
    private String timeSlotStart;
    private String timeSlotEnd;
    private String doctorName;
    private String scheduleDate;
}