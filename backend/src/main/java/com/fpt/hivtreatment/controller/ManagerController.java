package com.fpt.hivtreatment.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.fpt.hivtreatment.dto.AppointmentSlotDTO;
import com.fpt.hivtreatment.dto.DoctorScheduleDTO;
import com.fpt.hivtreatment.dto.UserResponse;
import com.fpt.hivtreatment.model.entity.Role;
import com.fpt.hivtreatment.payload.request.GenerateAppointmentSlotsRequest;
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

    private static final Logger logger = LoggerFactory.getLogger(ManagerController.class);

    private final DoctorScheduleService doctorScheduleService;
    private final UserManagementService userManagementService;
    private final AppointmentSlotService appointmentSlotService;
    private final DoctorProfileRepository doctorProfileRepository;

    /**
     * API lấy danh sách bác sĩ (từ bảng user)
     */
    @GetMapping("/doctors")
    public ResponseEntity<?> getAllDoctors(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {

        logger.info("Nhận yêu cầu lấy danh sách bác sĩ từ bảng user");

        try {
            // Tạo bộ lọc để lấy chỉ các user có vai trò là bác sĩ
            Map<String, Object> filters = new HashMap<>();
            filters.put("roleId", Role.ROLE_DOCTOR);
            filters.put("isActive", true);

            // Lấy danh sách bác sĩ
            List<UserResponse> doctors = userManagementService.getAllUsers(filters, page, size);
            long total = userManagementService.countUsers(filters);

            // Log chi tiết về danh sách bác sĩ
            logger.info("Tìm thấy {} bác sĩ", doctors.size());
            for (UserResponse doctor : doctors) {
                logger.info("Doctor: ID={}, Username={}, Name={}, RoleID={}",
                        doctor.getId(),
                        doctor.getUsername(),
                        doctor.getFullName(),
                        doctor.getRoleId());
            }

            // Tạo response
            Map<String, Object> response = new HashMap<>();
            response.put("doctors", doctors);
            response.put("totalItems", total);
            response.put("currentPage", page);
            response.put("totalPages", Math.ceil((double) total / size));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Lỗi khi lấy danh sách bác sĩ từ bảng user", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Lỗi khi lấy danh sách bác sĩ: " + e.getMessage()));
        }
    }

    /**
     * API tạo lịch làm việc cho bác sĩ
     */
    @PostMapping("/doctor-schedules")
    public ResponseEntity<?> createDoctorSchedule(@Valid @RequestBody DoctorScheduleDTO scheduleDTO) {
        logger.info("Nhận yêu cầu tạo lịch làm việc cho bác sĩ với id: {}", scheduleDTO.getDoctorId());
        logger.info("Chi tiết yêu cầu: {}", scheduleDTO);

        try {
            DoctorScheduleDTO createdSchedule = doctorScheduleService.createDoctorSchedule(scheduleDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdSchedule);
        } catch (Exception e) {
            logger.error("Lỗi khi tạo lịch làm việc cho bác sĩ", e);
            logger.error("Chi tiết lỗi: {}", e.getMessage());

            // Kiểm tra và log chi tiết hơn về các loại lỗi phổ biến
            if (e instanceof IllegalArgumentException) {
                logger.error("Dữ liệu không hợp lệ: {}", e.getMessage());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("message", "Dữ liệu không hợp lệ: " + e.getMessage()));
            } else if (e.getMessage() != null && e.getMessage().contains("doctor_id")) {
                logger.error("ID bác sĩ không hợp lệ: {}", scheduleDTO.getDoctorId());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("message", "ID bác sĩ không tồn tại hoặc không hợp lệ",
                                "details", e.getMessage()));
            } else if (e.getMessage() != null && e.getMessage().contains("work_shift_id")) {
                logger.error("ID ca làm việc không hợp lệ: {}", scheduleDTO.getWorkShiftId());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("message", "ID ca làm việc không tồn tại hoặc không hợp lệ",
                                "details", e.getMessage()));
            } else if (e.getMessage() != null && e.getMessage().contains("date")) {
                logger.error("Ngày không hợp lệ: {}", scheduleDTO.getScheduleDate());
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

        logger.info("Nhận yêu cầu lấy lịch làm việc của bác sĩ với id: {}, ngày: {}", doctorId, date);

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
            logger.error("Lỗi khi lấy lịch làm việc của bác sĩ", e);
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

        logger.info("Nhận yêu cầu lấy lịch làm việc của bác sĩ với id: {}, từ ngày: {} đến ngày: {}",
                doctorId, startDate, endDate);

        try {
            List<DoctorScheduleDTO> schedules = doctorScheduleService.getDoctorSchedulesByDateRange(
                    doctorId, startDate, endDate);

            return ResponseEntity.ok(schedules);
        } catch (Exception e) {
            logger.error("Lỗi khi lấy lịch làm việc của bác sĩ theo khoảng thời gian", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Lỗi khi lấy lịch làm việc của bác sĩ: " + e.getMessage()));
        }
    }

    /**
     * API xóa lịch làm việc của bác sĩ
     */
    @DeleteMapping("/doctor-schedules/{id}")
    public ResponseEntity<?> deleteDoctorSchedule(@PathVariable Long id) {
        logger.info("Nhận yêu cầu xóa lịch làm việc với id: {}", id);

        try {
            doctorScheduleService.deleteDoctorSchedule(id);
            return ResponseEntity.ok().body(Map.of("message", "Xóa lịch làm việc thành công"));
        } catch (ResourceNotFoundException e) {
            logger.error("Không tìm thấy lịch làm việc để xóa: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Không tìm thấy lịch làm việc: " + e.getMessage()));
        } catch (Exception e) {
            logger.error("Lỗi khi xóa lịch làm việc: {}", e.getMessage());
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

        logger.info("Nhận yêu cầu cập nhật lịch làm việc với id: {}", id);

        try {
            // Đảm bảo ID trong path và body trùng khớp
            if (!id.equals(scheduleDTO.getId())) {
                return ResponseEntity.badRequest()
                        .body(Map.of("message", "ID trong path và body không khớp"));
            }

            DoctorScheduleDTO updatedSchedule = doctorScheduleService.updateDoctorSchedule(id, scheduleDTO);
            return ResponseEntity.ok(updatedSchedule);
        } catch (ResourceNotFoundException e) {
            logger.error("Không tìm thấy lịch làm việc để cập nhật: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Không tìm thấy lịch làm việc: " + e.getMessage()));
        } catch (Exception e) {
            logger.error("Lỗi khi cập nhật lịch làm việc: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Lỗi khi cập nhật lịch làm việc: " + e.getMessage()));
        }
    }

    /**
     * API tạo các appointment slots từ lịch làm việc của bác sĩ
     */
    @PostMapping("/appointment-slots/generate")
    public ResponseEntity<?> generateAppointmentSlots(@Valid @RequestBody GenerateAppointmentSlotsRequest request) {
        logger.info("Nhận yêu cầu tạo các slot khám bệnh cho lịch bác sĩ: {}", request.getDoctor_schedule_id());

        try {
            List<AppointmentSlotDTO> createdSlots = appointmentSlotService.generateAppointmentSlots(request);

            Map<String, Object> response = new HashMap<>();
            response.put("slots", createdSlots);
            response.put("totalSlots", createdSlots.size());
            response.put("message", "Tạo " + createdSlots.size() + " slot khám bệnh thành công");

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            logger.error("Lỗi khi tạo các slot khám bệnh", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Lỗi khi tạo các slot khám bệnh: " + e.getMessage()));
        }
    }

    /**
     * API lấy danh sách appointment slots theo doctor schedule
     */
    @GetMapping("/appointment-slots/by-schedule/{doctorScheduleId}")
    public ResponseEntity<?> getAppointmentSlotsBySchedule(@PathVariable Long doctorScheduleId) {
        logger.info("Nhận yêu cầu lấy danh sách slot khám bệnh theo lịch bác sĩ ID: {}", doctorScheduleId);

        try {
            List<AppointmentSlotDTO> slots = appointmentSlotService.getSlotsByScheduleId(doctorScheduleId);

            Map<String, Object> response = new HashMap<>();
            response.put("slots", slots);
            response.put("totalSlots", slots.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Lỗi khi lấy danh sách slot khám bệnh", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Lỗi khi lấy danh sách slot khám bệnh: " + e.getMessage()));
        }
    }

    /**
     * API lấy tổng quan dashboard
     */
    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboardOverview() {
        logger.info("Nhận yêu cầu lấy tổng quan dashboard cho manager");

        try {
            // Lấy các thông tin tổng quan
            Map<String, Object> dashboardInfo = new HashMap<>();

            // Tổng số lịch làm việc của bác sĩ
            long totalSchedules = doctorScheduleService.countDoctorSchedules(null, null);
            dashboardInfo.put("totalDoctorSchedules", totalSchedules);

            return ResponseEntity.ok(dashboardInfo);
        } catch (Exception e) {
            logger.error("Lỗi khi lấy tổng quan dashboard", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Lỗi khi lấy tổng quan dashboard: " + e.getMessage()));
        }
    }

    /**
     * API lấy danh sách bác sĩ từ bảng doctor_profile
     */
    @GetMapping("/doctor-profiles")
    public ResponseEntity<?> getDoctorProfiles() {
        logger.info("Nhận yêu cầu lấy danh sách bác sĩ từ bảng doctor_profile");

        try {
            List<DoctorProfile> doctorProfiles = doctorProfileRepository.findAll();

            logger.info("Tìm thấy {} bác sĩ từ bảng doctor_profile", doctorProfiles.size());
            for (DoctorProfile doctor : doctorProfiles) {
                logger.info("DoctorProfile: ID={}, UserID={}, Name={}",
                        doctor.getDoctorId(),
                        doctor.getUser().getId(),
                        doctor.getUser().getFullName());
            }

            return ResponseEntity.ok(doctorProfiles);
        } catch (Exception e) {
            logger.error("Lỗi khi lấy danh sách bác sĩ từ bảng doctor_profile", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Lỗi khi lấy danh sách bác sĩ: " + e.getMessage()));
        }
    }
}