package com.fpt.hivtreatment.service;

import java.time.LocalDate;
import java.util.List;

import com.fpt.hivtreatment.dto.DoctorScheduleDTO;
import com.fpt.hivtreatment.model.entity.DoctorSchedule;
import com.fpt.hivtreatment.payload.request.DoctorScheduleRequest;
import com.fpt.hivtreatment.payload.response.DoctorScheduleResponse;

public interface DoctorScheduleService {

    /**
     * Tạo lịch làm việc cho bác sĩ
     * 
     * @param scheduleDTO Thông tin lịch làm việc
     * @return Lịch làm việc đã tạo
     */
    DoctorScheduleDTO createDoctorSchedule(DoctorScheduleDTO scheduleDTO);

    /**
     * Lấy lịch làm việc của bác sĩ theo bộ lọc
     * 
     * @param doctorId ID của bác sĩ (có thể null)
     * @param date     Ngày (có thể null)
     * @param page     Số trang
     * @param size     Kích thước trang
     * @return Danh sách lịch làm việc
     */
    List<DoctorScheduleDTO> getDoctorSchedules(Long doctorId, String date, int page, int size);

    /**
     * Lấy lịch làm việc của bác sĩ theo khoảng thời gian
     * 
     * @param doctorId  ID của bác sĩ
     * @param startDate Ngày bắt đầu (định dạng YYYY-MM-DD)
     * @param endDate   Ngày kết thúc (định dạng YYYY-MM-DD)
     * @return Danh sách lịch làm việc
     */
    List<DoctorScheduleDTO> getDoctorSchedulesByDateRange(Long doctorId, String startDate, String endDate);

    /**
     * Đếm số lượng lịch làm việc theo bộ lọc
     * 
     * @param doctorId ID của bác sĩ (có thể null)
     * @param date     Ngày (có thể null)
     * @return Số lượng lịch làm việc
     */
    long countDoctorSchedules(Long doctorId, String date);

    /**
     * Lấy chi tiết lịch làm việc theo ID
     * 
     * @param id ID của lịch làm việc
     * @return Chi tiết lịch làm việc
     */
    DoctorScheduleDTO getDoctorScheduleById(Long id);

    /**
     * Cập nhật lịch làm việc
     * 
     * @param id          ID của lịch làm việc
     * @param scheduleDTO Thông tin cập nhật
     * @return Lịch làm việc đã cập nhật
     */
    DoctorScheduleDTO updateDoctorSchedule(Long id, DoctorScheduleDTO scheduleDTO);

    /**
     * Xóa lịch làm việc
     * 
     * @param id ID của lịch làm việc
     */
    void deleteDoctorSchedule(Long id);

    /**
     * Tạo lịch làm việc (phương thức cũ)
     * 
     * @param request Thông tin lịch làm việc
     * @return Thông tin lịch làm việc đã tạo
     */
    DoctorScheduleResponse createSchedule(DoctorScheduleRequest request);

    /**
     * Lấy lịch làm việc theo bác sĩ (phương thức cũ)
     * 
     * @param doctorId ID của bác sĩ
     * @return Danh sách lịch làm việc
     */
    List<DoctorScheduleResponse> getSchedulesByDoctor(Long doctorId);

    /**
     * Lấy lịch làm việc theo bác sĩ và ngày (phương thức cũ)
     * 
     * @param doctorId ID của bác sĩ
     * @param date     Ngày
     * @return Danh sách lịch làm việc
     */
    List<DoctorScheduleResponse> getSchedulesByDoctorAndDate(Long doctorId, LocalDate date);

    /**
     * Lấy lịch làm việc trong tương lai (phương thức cũ)
     * 
     * @return Danh sách lịch làm việc
     */
    List<DoctorScheduleResponse> getFutureSchedules();

    /**
     * Lấy chi tiết lịch làm việc theo ID (phương thức cũ)
     * 
     * @param scheduleId ID của lịch làm việc
     * @return Chi tiết lịch làm việc
     */
    DoctorSchedule getScheduleById(Long scheduleId);
}