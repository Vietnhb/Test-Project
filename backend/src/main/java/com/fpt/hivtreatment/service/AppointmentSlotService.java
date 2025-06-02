package com.fpt.hivtreatment.service;

import com.fpt.hivtreatment.dto.AppointmentSlotDTO;
import com.fpt.hivtreatment.payload.request.GenerateAppointmentSlotsRequest;

import java.util.List;

public interface AppointmentSlotService {
    /**
     * Tạo các slot cho lịch của bác sĩ dựa trên ca làm việc
     * 
     * @param request thông tin request chứa mã lịch, bác sĩ và ca làm việc
     * @return danh sách các slot đã tạo
     */
    List<AppointmentSlotDTO> generateAppointmentSlots(GenerateAppointmentSlotsRequest request);

    /**
     * Lấy danh sách các slot theo mã lịch bác sĩ
     * 
     * @param doctorScheduleId mã lịch bác sĩ
     * @return danh sách các slot
     */
    List<AppointmentSlotDTO> getSlotsByScheduleId(Long doctorScheduleId);

    /**
     * Lấy danh sách các slot của bác sĩ theo ngày
     * 
     * @param doctorId mã bác sĩ
     * @param date     ngày cần lấy slot
     * @return danh sách các slot
     */
    List<AppointmentSlotDTO> getSlotsByDoctorAndDate(Long doctorId, String date);
}