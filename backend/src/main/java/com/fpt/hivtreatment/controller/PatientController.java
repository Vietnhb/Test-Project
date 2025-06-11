package com.fpt.hivtreatment.controller;

import com.fpt.hivtreatment.dto.AppointmentSlotDTO;
import com.fpt.hivtreatment.service.AppointmentSlotService;
import com.fpt.hivtreatment.service.DoctorScheduleService;
import com.fpt.hivtreatment.payload.response.DoctorScheduleResponse;
import com.fpt.hivtreatment.service.AppointmentService;
import com.fpt.hivtreatment.payload.request.AppointmentRequest;
import com.fpt.hivtreatment.payload.response.AppointmentResponse;
import com.fpt.hivtreatment.security.services.UserDetailsImpl;
import com.fpt.hivtreatment.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller cho bệnh nhân
 * Cung cấp các API dành cho bệnh nhân để truy vấn thông tin appointment slots
 */
@RestController
@RequestMapping("/api/patient")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('1')") // Patient role (role_id = 1)
public class PatientController {

    private final AppointmentSlotService appointmentSlotService;
    private final DoctorScheduleService doctorScheduleService;
    private final AppointmentService appointmentService;

    /**
     * API lấy danh sách appointment slots có sẵn theo bác sĩ và ngày
     * 
     * @param doctorId ID của bác sĩ
     * @param date     Ngày cần lấy slots (format: YYYY-MM-DD)
     * @return Danh sách appointment slots có sẵn
     */
    @GetMapping("/appointment-slots/available")
    public ResponseEntity<?> getAvailableAppointmentSlots(
            @RequestParam Long doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) String date) {

        try {
            List<AppointmentSlotDTO> slots = appointmentSlotService.getSlotsByDoctorAndDate(doctorId, date);

            // Lọc chỉ những slots có sẵn (isAvailable = true)
            List<AppointmentSlotDTO> availableSlots = slots.stream()
                    .filter(slot -> slot.getIsAvailable() != null && slot.getIsAvailable())
                    .toList();

            Map<String, Object> response = new HashMap<>();
            response.put("slots", availableSlots);
            response.put("totalAvailableSlots", availableSlots.size());
            response.put("doctorId", doctorId);
            response.put("date", date);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Lỗi khi lấy danh sách slot khám bệnh: " + e.getMessage()));
        }
    }

    /**
     * API lấy danh sách appointment slots theo bác sĩ và ngày (bao gồm cả đã đặt và
     * chưa đặt)
     * 
     * @param doctorId ID của bác sĩ
     * @param date     Ngày cần lấy slots (format: YYYY-MM-DD)
     * @return Danh sách tất cả appointment slots
     */
    @GetMapping("/appointment-slots")
    public ResponseEntity<?> getAppointmentSlots(
            @RequestParam Long doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) String date) {

        try {
            List<AppointmentSlotDTO> slots = appointmentSlotService.getSlotsByDoctorAndDate(doctorId, date);

            Map<String, Object> response = new HashMap<>();
            response.put("slots", slots);
            response.put("totalSlots", slots.size());
            response.put("doctorId", doctorId);
            response.put("date", date);

            long availableCount = slots.stream()
                    .filter(slot -> slot.getIsAvailable() != null && slot.getIsAvailable())
                    .count();
            response.put("availableCount", availableCount);
            response.put("bookedCount", slots.size() - availableCount);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Lỗi khi lấy danh sách slot khám bệnh: " + e.getMessage()));
        }
    }

    /**
     * API lấy danh sách các ngày làm việc của bác sĩ
     * 
     * @param doctorId ID của bác sĩ
     * @return Danh sách lịch làm việc của bác sĩ
     */
    @GetMapping("/doctor-schedules/{doctorId}")
    public ResponseEntity<?> getDoctorSchedules(@PathVariable Long doctorId) {
        try {
            List<DoctorScheduleResponse> schedules = doctorScheduleService.getSchedulesByDoctor(doctorId);

            Map<String, Object> response = new HashMap<>();
            response.put("schedules", schedules);
            response.put("totalSchedules", schedules.size());
            response.put("doctorId", doctorId);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Lỗi khi lấy lịch làm việc của bác sĩ: " + e.getMessage()));
        }
    }

    /**
     * API lấy lịch làm việc của bác sĩ theo ngày cụ thể
     * 
     * @param doctorId ID của bác sĩ
     * @param date     Ngày cần lấy lịch (format: YYYY-MM-DD)
     * @return Lịch làm việc của bác sĩ trong ngày
     */
    @GetMapping("/doctor-schedules/{doctorId}/date/{date}")
    public ResponseEntity<?> getDoctorScheduleByDate(
            @PathVariable Long doctorId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        try {
            List<DoctorScheduleResponse> schedules = doctorScheduleService.getSchedulesByDoctorAndDate(doctorId, date);

            Map<String, Object> response = new HashMap<>();
            response.put("schedules", schedules);
            response.put("totalSchedules", schedules.size());
            response.put("doctorId", doctorId);
            response.put("date", date);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Lỗi khi lấy lịch làm việc của bác sĩ: " + e.getMessage()));
        }
    }

    /**
     * API lấy tất cả lịch làm việc tương lai
     * 
     * @return Danh sách lịch làm việc tương lai
     */
    @GetMapping("/doctor-schedules/future")
    public ResponseEntity<?> getFutureSchedules() {
        try {
            List<DoctorScheduleResponse> schedules = doctorScheduleService.getFutureSchedules();

            Map<String, Object> response = new HashMap<>();
            response.put("schedules", schedules);
            response.put("totalSchedules", schedules.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Lỗi khi lấy danh sách lịch làm việc tương lai: " + e.getMessage()));
        }
    }

    /**
     * API cho phép bệnh nhân xem danh sách lịch hẹn của mình
     * Có thể lọc theo trạng thái
     * 
     * @param status Trạng thái lịch hẹn (Chờ xác nhận, Đã xác nhận, Đã hủy, Hoàn
     *               thành)
     * @return Danh sách lịch hẹn của bệnh nhân
     */
    @GetMapping("/my-appointments")
    public ResponseEntity<?> getMyAppointments(
            @RequestParam(required = false) String status) {

        try {
            // Lấy thông tin người dùng đang đăng nhập
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            // Extract UserDetails from authentication
            Long userId = null;
            if (authentication.getPrincipal() instanceof UserDetailsImpl) {
                UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
                userId = userDetails.getId();
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "Không thể xác thực người dùng. Vui lòng đăng nhập lại."));
            }

            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "Không thể xác thực người dùng. Vui lòng đăng nhập lại."));
            }

            List<AppointmentResponse> appointments;

            try {
                // Lấy danh sách lịch hẹn theo trạng thái (nếu có)
                if (status != null && !status.isEmpty() && !status.equals("all")) {
                    appointments = appointmentService.getAppointmentsByPatientAndStatus(userId, status);
                } else {
                    appointments = appointmentService.getAppointmentsByPatient(userId);
                }
            } catch (ResourceNotFoundException e) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "Không tìm thấy thông tin bệnh nhân với ID: " + userId));
            }

            Map<String, Object> response = new HashMap<>();
            response.put("appointments", appointments);
            response.put("totalItems", appointments.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Lỗi khi lấy danh sách lịch hẹn: " + e.getMessage());
            errorResponse.put("details", e.toString());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse);
        }
    }

    /**
     * API đặt lịch hẹn khám bệnh
     * 
     * @param request Thông tin lịch hẹn
     * @return Thông tin lịch hẹn đã đặt
     */
    @PostMapping("/appointments")
    public ResponseEntity<?> createAppointment(@RequestBody AppointmentRequest request) {
        try {
            AppointmentResponse result = appointmentService.createAppointment(request);

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "message", "Lỗi khi đặt lịch hẹn: " + e.getMessage()));
        }
    }
}