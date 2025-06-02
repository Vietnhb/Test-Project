package com.fpt.hivtreatment.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DoctorScheduleDTO {
    private Long id;
    private Long doctorId;
    private String doctorName;

    // Thêm định dạng JSON và deserializer
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate scheduleDate;

    private LocalTime startTime;
    private LocalTime endTime;
    private Long workShiftId;
    private String notes;
    private Boolean isAvailable;

    // Thêm trường phụ để hỗ trợ nhận date dạng String từ frontend
    private String schedule_date;

    public LocalDate getScheduleDate() {
        // Nếu scheduleDate là null nhưng có schedule_date thì parse
        if (this.scheduleDate == null && this.schedule_date != null && !this.schedule_date.isEmpty()) {
            try {
                return LocalDate.parse(this.schedule_date);
            } catch (Exception e) {
                // Nếu parse lỗi, trả về null
                return null;
            }
        }
        return this.scheduleDate;
    }
}