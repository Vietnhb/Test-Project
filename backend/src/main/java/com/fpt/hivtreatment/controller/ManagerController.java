package com.fpt.hivtreatment.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.fpt.hivtreatment.dto.AppointmentSlotDTO;
import com.fpt.hivtreatment.dto.DoctorScheduleDTO;
import com.fpt.hivtreatment.dto.UserResponse;
import com.fpt.hivtreatment.model.entity.Role;
import com.fpt.hivtreatment.payload.request.GenerateAppointmentSlotsRequest;
import com.fpt.hivtreatment.service.AppointmentService;
import com.fpt.hivtreatment.service.AppointmentSlotService;
import com.fpt.hivtreatment.service.DoctorScheduleService;
import com.fpt.hivtreatment.service.UserManagementService;
import com.fpt.hivtreatment.exception.ResourceNotFoundException;
import com.fpt.hivtreatment.model.entity.DoctorProfile;
import com.fpt.hivtreatment.repository.DoctorProfileRepository;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Controller quản lý hệ thống dành cho Manager
 * API dành cho manager quản lý lịch bác sĩ
 */
@RestController
@RequestMapping("/api/manager")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('5')") // Manager (role_id = 5) có quyền truy cập
public class ManagerController {

    private final DoctorScheduleService doctorScheduleService;
    private final UserManagementService userManagementService;
    private final AppointmentSlotService appointmentSlotService;
    private final DoctorProfileRepository doctorProfileRepository;
    private final AppointmentService appointmentService;

    /**
     * API lấy danh sách bác sĩ (từ bảng user)
     */
    @GetMapping("/doctors")
    public ResponseEntity<?> getAllDoctors(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {

        try {
            // Tạo bộ lọc để lấy chỉ các user có vai trò là bác sĩ
            Map<String, Object> filters = new HashMap<>();
            filters.put("roleId", Role.ROLE_DOCTOR);
            filters.put("isActive", true);

            // Lấy danh sách bác sĩ
            List<UserResponse> doctors = userManagementService.getAllUsers(filters, page, size);
            long total = userManagementService.countUsers(filters);

            // Tạo response
            Map<String, Object> response = new HashMap<>();
            response.put("doctors", doctors);
            response.put("totalItems", total);
            response.put("currentPage", page);
            response.put("totalPages", Math.ceil((double) total / size));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Lỗi khi lấy danh sách bác sĩ: " + e.getMessage()));
        }
    }

    /**
     * API tạo lịch làm việc cho bác sĩ
     */
    @PostMapping("/doctor-schedules")
    public ResponseEntity<?> createDoctorSchedule(@Valid @RequestBody DoctorScheduleDTO scheduleDTO) {
        try {
            DoctorScheduleDTO createdSchedule = doctorScheduleService.createDoctorSchedule(scheduleDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdSchedule);
        } catch (Exception e) {
            // Kiểm tra và log chi tiết hơn về các loại lỗi phổ biến
            if (e instanceof IllegalArgumentException) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("message", "Dữ liệu không hợp lệ: " + e.getMessage()));
            } else if (e.getMessage() != null && e.getMessage().contains("doctor_id")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("message", "ID bác sĩ không tồn tại hoặc không hợp lệ",
                                "details", e.getMessage()));
            } else if (e.getMessage() != null && e.getMessage().contains("work_shift_id")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("message", "ID ca làm việc không tồn tại hoặc không hợp lệ",
                                "details", e.getMessage()));
            } else if (e.getMessage() != null && e.getMessage().contains("date")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("message", "Ngày làm việc không hợp lệ hoặc định dạng không đúng",
                                "details", e.getMessage()));
            }

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Lỗi khi tạo lịch làm việc: " + e.getMessage()));
        }
    }

    /**
     * API lấy lịch làm việc của bác sĩ
     */
    @GetMapping("/doctor-schedules")
    public ResponseEntity<?> getDoctorSchedules(
            @RequestParam(required = false) Long doctorId,
            @RequestParam(required = false) String date,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        try {
            List<DoctorScheduleDTO> schedules = doctorScheduleService.getDoctorSchedules(doctorId, date, page, size);
            long total = doctorScheduleService.countDoctorSchedules(doctorId, date);

            Map<String, Object> response = new HashMap<>();
            response.put("schedules", schedules);
            response.put("totalItems", total);
            response.put("currentPage", page);
            response.put("totalPages", Math.ceil((double) total / size));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Lỗi khi lấy lịch làm việc của bác sĩ: " + e.getMessage()));
        }
    }

    /**
     * API lấy lịch làm việc của bác sĩ theo khoảng thời gian
     */
    @GetMapping("/doctor-schedules/date-range")
    public ResponseEntity<?> getDoctorSchedulesByDateRange(
            @RequestParam(required = true) Long doctorId,
            @RequestParam(required = true) String startDate,
            @RequestParam(required = true) String endDate) {

        try {
            List<DoctorScheduleDTO> schedules = doctorScheduleService.getDoctorSchedulesByDateRange(
                    doctorId, startDate, endDate);

            return ResponseEntity.ok(schedules);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Lỗi khi lấy lịch làm việc của bác sĩ: " + e.getMessage()));
        }
    }

    /**
     * API xóa lịch làm việc của bác sĩ
     */
    @DeleteMapping("/doctor-schedules/{id}")
    public ResponseEntity<?> deleteDoctorSchedule(@PathVariable Long id) {
        try {
            doctorScheduleService.deleteDoctorSchedule(id);
            return ResponseEntity.ok().body(Map.of("message", "Xóa lịch làm việc thành công"));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Không tìm thấy lịch làm việc: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Lỗi khi xóa lịch làm việc: " + e.getMessage()));
        }
    }

    /**
     * API cập nhật lịch làm việc của bác sĩ
     */
    @PutMapping("/doctor-schedules/{id}")
    public ResponseEntity<?> updateDoctorSchedule(
            @PathVariable Long id,
            @Valid @RequestBody DoctorScheduleDTO scheduleDTO) {

        try {
            // Đảm bảo ID trong path và body trùng khớp
            if (!id.equals(scheduleDTO.getId())) {
                return ResponseEntity.badRequest()
                        .body(Map.of("message", "ID trong path và body không khớp"));
            }

            DoctorScheduleDTO updatedSchedule = doctorScheduleService.updateDoctorSchedule(id, scheduleDTO);
            return ResponseEntity.ok(updatedSchedule);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Không tìm thấy lịch làm việc: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Lỗi khi cập nhật lịch làm việc: " + e.getMessage()));
        }
    }

    /**
     * API tạo các appointment slots từ lịch làm việc của bác sĩ
     */
    @PostMapping("/appointment-slots/generate")
    public ResponseEntity<?> generateAppointmentSlots(@Valid @RequestBody GenerateAppointmentSlotsRequest request) {
        try {
            List<AppointmentSlotDTO> createdSlots = appointmentSlotService.generateAppointmentSlots(request);

            Map<String, Object> response = new HashMap<>();
            response.put("slots", createdSlots);
            response.put("totalSlots", createdSlots.size());
            response.put("message", "Tạo " + createdSlots.size() + " slot khám bệnh thành công");

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Lỗi khi tạo các slot khám bệnh: " + e.getMessage()));
        }
    }

    /**
     * API lấy danh sách appointment slots theo doctor schedule
     */
    @GetMapping("/appointment-slots/by-schedule/{doctorScheduleId}")
    public ResponseEntity<?> getAppointmentSlotsBySchedule(@PathVariable Long doctorScheduleId) {
        try {
            List<AppointmentSlotDTO> slots = appointmentSlotService.getSlotsByScheduleId(doctorScheduleId);

            Map<String, Object> response = new HashMap<>();
            response.put("slots", slots);
            response.put("totalSlots", slots.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Lỗi khi lấy danh sách slot khám bệnh: " + e.getMessage()));
        }
    }

    /**
     * API lấy tổng quan dashboard
     */
    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboardOverview() {
        try {
            // Lấy các thông tin tổng quan
            Map<String, Object> dashboardInfo = new HashMap<>();

            // Tổng số lịch làm việc của bác sĩ
            long totalSchedules = doctorScheduleService.countDoctorSchedules(null, null);
            dashboardInfo.put("totalDoctorSchedules", totalSchedules);

            return ResponseEntity.ok(dashboardInfo);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Lỗi khi lấy tổng quan dashboard: " + e.getMessage()));
        }
    }

    /**
     * API lấy danh sách bác sĩ từ bảng doctor_profile
     */
    @GetMapping("/doctor-profiles")
    public ResponseEntity<?> getDoctorProfiles() {
        try {
            List<DoctorProfile> doctorProfiles = doctorProfileRepository.findAll();

            return ResponseEntity.ok(doctorProfiles);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Lỗi khi lấy danh sách bác sĩ: " + e.getMessage()));
        }
    }

    /**
     * API lấy thống kê lịch hẹn
     */
    @GetMapping("/appointment-stats")
    public ResponseEntity<?> getAppointmentStatistics() {
        try {
            Map<String, Object> stats = new HashMap<>();

            // Lấy số lượng lịch hẹn theo trạng thái
            long pendingCount = appointmentService.countAppointmentsByStatus("Chờ xác nhận");
            long approvedCount = appointmentService.countAppointmentsByStatus("Đã xác nhận");
            long cancelledCount = appointmentService.countAppointmentsByStatus("Đã hủy");
            long completedCount = appointmentService.countAppointmentsByStatus("Hoàn thành");

            // Tính tổng số lịch hẹn
            long totalCount = pendingCount + approvedCount + cancelledCount + completedCount;

            // Đóng gói kết quả
            stats.put("pendingCount", pendingCount);
            stats.put("approvedCount", approvedCount);
            stats.put("cancelledCount", cancelledCount);
            stats.put("completedCount", completedCount);
            stats.put("totalCount", totalCount);

            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Lỗi khi lấy thống kê lịch hẹn: " + e.getMessage()));
        }
    }

    /**
     * API lấy danh sách lịch hẹn theo trạng thái hoặc khoảng thời gian
     */
    @GetMapping("/appointments")
    public ResponseEntity<?> getAppointments(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "5") int limit) {

        try {
            Map<String, Object> result = appointmentService.getAppointmentsForManager(status, startDate, endDate, page,
                    size, limit);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Lỗi khi lấy danh sách lịch hẹn: " + e.getMessage()));
        }
    }

    /**
     * API cập nhật trạng thái lịch hẹn
     */
    @PutMapping("/appointments/{id}/status")
    public ResponseEntity<?> updateAppointmentStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> statusUpdate) {

        String status = statusUpdate.get("status");
        String cancellationReason = statusUpdate.get("cancellationReason");

        try {
            Map<String, Object> result = appointmentService.updateAppointmentStatus(id, status, cancellationReason);
            return ResponseEntity.ok(result);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Không tìm thấy lịch hẹn: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Lỗi khi cập nhật trạng thái lịch hẹn: " + e.getMessage()));
        }
    }
}