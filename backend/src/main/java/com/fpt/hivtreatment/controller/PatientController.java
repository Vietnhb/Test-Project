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
<<<<<<< HEAD
=======
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
>>>>>>> fd42c148e0431975301ca683137e9cc7dea64a1c
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

<<<<<<< HEAD
=======
    private static final Logger logger = LoggerFactory.getLogger(PatientController.class);

>>>>>>> fd42c148e0431975301ca683137e9cc7dea64a1c
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

<<<<<<< HEAD
=======
        logger.info("Patient request: Getting available appointment slots for doctor ID: {} on date: {}", doctorId,
                date);

>>>>>>> fd42c148e0431975301ca683137e9cc7dea64a1c
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

<<<<<<< HEAD
            return ResponseEntity.ok(response);
        } catch (Exception e) {
=======
            logger.info("Found {} available appointment slots for doctor ID: {} on date: {}",
                    availableSlots.size(), doctorId, date);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error getting available appointment slots for doctor ID: {} on date: {}", doctorId, date, e);
>>>>>>> fd42c148e0431975301ca683137e9cc7dea64a1c
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

<<<<<<< HEAD
=======
        logger.info("Patient request: Getting all appointment slots for doctor ID: {} on date: {}", doctorId, date);

>>>>>>> fd42c148e0431975301ca683137e9cc7dea64a1c
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

<<<<<<< HEAD
            return ResponseEntity.ok(response);
        } catch (Exception e) {
=======
            logger.info("Found {} appointment slots ({} available, {} booked) for doctor ID: {} on date: {}",
                    slots.size(), availableCount, slots.size() - availableCount, doctorId, date);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error getting appointment slots for doctor ID: {} on date: {}", doctorId, date, e);
>>>>>>> fd42c148e0431975301ca683137e9cc7dea64a1c
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
<<<<<<< HEAD
=======
        logger.info("Patient request: Getting schedules for doctor ID: {}", doctorId);

>>>>>>> fd42c148e0431975301ca683137e9cc7dea64a1c
        try {
            List<DoctorScheduleResponse> schedules = doctorScheduleService.getSchedulesByDoctor(doctorId);

            Map<String, Object> response = new HashMap<>();
            response.put("schedules", schedules);
            response.put("totalSchedules", schedules.size());
            response.put("doctorId", doctorId);

<<<<<<< HEAD
            return ResponseEntity.ok(response);
        } catch (Exception e) {
=======
            logger.info("Found {} schedules for doctor ID: {}", schedules.size(), doctorId);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error getting doctor schedules for doctor ID: {}", doctorId, e);
>>>>>>> fd42c148e0431975301ca683137e9cc7dea64a1c
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

<<<<<<< HEAD
=======
        logger.info("Patient request: Getting schedule for doctor ID: {} on date: {}", doctorId, date);

>>>>>>> fd42c148e0431975301ca683137e9cc7dea64a1c
        try {
            List<DoctorScheduleResponse> schedules = doctorScheduleService.getSchedulesByDoctorAndDate(doctorId, date);

            Map<String, Object> response = new HashMap<>();
            response.put("schedules", schedules);
            response.put("totalSchedules", schedules.size());
            response.put("doctorId", doctorId);
            response.put("date", date);

<<<<<<< HEAD
            return ResponseEntity.ok(response);
        } catch (Exception e) {
=======
            logger.info("Found {} schedules for doctor ID: {} on date: {}", schedules.size(), doctorId, date);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error getting doctor schedule for doctor ID: {} on date: {}", doctorId, date, e);
>>>>>>> fd42c148e0431975301ca683137e9cc7dea64a1c
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
<<<<<<< HEAD
=======
        logger.info("Patient request: Getting all future schedules");

>>>>>>> fd42c148e0431975301ca683137e9cc7dea64a1c
        try {
            List<DoctorScheduleResponse> schedules = doctorScheduleService.getFutureSchedules();

            Map<String, Object> response = new HashMap<>();
            response.put("schedules", schedules);
            response.put("totalSchedules", schedules.size());

<<<<<<< HEAD
            return ResponseEntity.ok(response);
        } catch (Exception e) {
=======
            logger.info("Found {} future schedules", schedules.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error getting future schedules", e);
>>>>>>> fd42c148e0431975301ca683137e9cc7dea64a1c
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

<<<<<<< HEAD
=======
            // Log authentication details for debugging
            logger.info("Authentication details - Principal type: {}",
                    authentication.getPrincipal() != null ? authentication.getPrincipal().getClass().getName()
                            : "null");
            logger.info("Authentication details - Name: {}", authentication.getName());
            logger.info("Authentication details - Authorities: {}", authentication.getAuthorities());

>>>>>>> fd42c148e0431975301ca683137e9cc7dea64a1c
            // Extract UserDetails from authentication
            Long userId = null;
            if (authentication.getPrincipal() instanceof UserDetailsImpl) {
                UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
                userId = userDetails.getId();
<<<<<<< HEAD
            } else {
=======
                logger.info("Successfully extracted user ID from UserDetailsImpl: {}", userId);
            } else {
                logger.error("Principal is not instance of UserDetailsImpl but: {}",
                        authentication.getPrincipal() != null ? authentication.getPrincipal().getClass().getName()
                                : "null");
>>>>>>> fd42c148e0431975301ca683137e9cc7dea64a1c
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "Không thể xác thực người dùng. Vui lòng đăng nhập lại."));
            }

            if (userId == null) {
<<<<<<< HEAD
=======
                logger.error("Could not determine user ID from authentication");
>>>>>>> fd42c148e0431975301ca683137e9cc7dea64a1c
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "Không thể xác thực người dùng. Vui lòng đăng nhập lại."));
            }

<<<<<<< HEAD
=======
            logger.info("Patient request: Getting appointments for authenticated user with ID {}, status: {}",
                    userId, status != null ? status : "all");

>>>>>>> fd42c148e0431975301ca683137e9cc7dea64a1c
            List<AppointmentResponse> appointments;

            try {
                // Lấy danh sách lịch hẹn theo trạng thái (nếu có)
                if (status != null && !status.isEmpty() && !status.equals("all")) {
                    appointments = appointmentService.getAppointmentsByPatientAndStatus(userId, status);
<<<<<<< HEAD
                } else {
                    appointments = appointmentService.getAppointmentsByPatient(userId);
                }
            } catch (ResourceNotFoundException e) {
=======
                    logger.info("Found {} appointments with status {} for patient ID {}",
                            appointments.size(), status, userId);
                } else {
                    appointments = appointmentService.getAppointmentsByPatient(userId);
                    logger.info("Found {} total appointments for patient ID {}", appointments.size(), userId);
                }
            } catch (ResourceNotFoundException e) {
                logger.error("Patient not found with ID: {}", userId, e);
>>>>>>> fd42c148e0431975301ca683137e9cc7dea64a1c
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "Không tìm thấy thông tin bệnh nhân với ID: " + userId));
            }

            Map<String, Object> response = new HashMap<>();
            response.put("appointments", appointments);
            response.put("totalItems", appointments.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
<<<<<<< HEAD
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Lỗi khi lấy danh sách lịch hẹn: " + e.getMessage());
            errorResponse.put("details", e.toString());
=======
            logger.error("Error retrieving patient appointments", e);
            String stackTrace = "";
            for (StackTraceElement element : e.getStackTrace()) {
                stackTrace += element.toString() + "\n";
            }

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Lỗi khi lấy danh sách lịch hẹn: " + e.getMessage());
            errorResponse.put("details", e.toString());
            errorResponse.put("stackTrace", stackTrace);
>>>>>>> fd42c148e0431975301ca683137e9cc7dea64a1c

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
<<<<<<< HEAD
        try {
            AppointmentResponse result = appointmentService.createAppointment(request);

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "message", "Lỗi khi đặt lịch hẹn: " + e.getMessage()));
=======
        logger.info("Patient request: Creating appointment for patient ID: {}, doctor ID: {}, slot ID: {}",
                request.getPatientId(), request.getDoctorId(), request.getAppointmentSlotId());

        // Debug the incoming request
        logger.debug("Full appointment request: {}", request);
        if (request.getAppointmentDate() != null) {
            logger.info("Appointment date received: {}", request.getAppointmentDate());
        } else {
            logger.warn("Appointment date is null in the request");
        }

        try {
            AppointmentResponse result = appointmentService.createAppointment(request);

            logger.info("Successfully created appointment for patient ID: {}", request.getPatientId());

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("Error creating appointment", e);
            String stackTrace = "";
            for (StackTraceElement element : e.getStackTrace()) {
                stackTrace += element.toString() + "\n";
            }

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "message", "Lỗi khi đặt lịch hẹn: " + e.getMessage(),
                            "details", e.toString(),
                            "stackTrace", stackTrace));
>>>>>>> fd42c148e0431975301ca683137e9cc7dea64a1c
        }
    }
}