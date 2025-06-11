package com.fpt.hivtreatment.controller;

import com.fpt.hivtreatment.dto.AppointmentSlotDTO;
import com.fpt.hivtreatment.service.AppointmentSlotService;
import com.fpt.hivtreatment.repository.AppointmentSlotRepository;
import com.fpt.hivtreatment.model.entity.AppointmentSlot;
import com.fpt.hivtreatment.model.entity.DoctorProfile;
import com.fpt.hivtreatment.repository.DoctorProfileRepository;
import com.fpt.hivtreatment.repository.DoctorScheduleRepository;
import com.fpt.hivtreatment.model.entity.DoctorSchedule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * Controller for appointment slots for patients to book
 */
@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AppointmentSlotController {
        private final AppointmentSlotService appointmentSlotService;
        private final AppointmentSlotRepository appointmentSlotRepository; // Direct repository access
        private final DoctorProfileRepository doctorProfileRepository; // For doctor lookup
        private final DoctorScheduleRepository doctorScheduleRepository; // For schedule lookup

        @Autowired
        private JdbcTemplate jdbcTemplate; // For direct SQL queries

        /**
         * Get available appointment slots for a doctor on a specific date
         * This endpoint is called by the frontend BookAppointment component
         * 
         * @param doctorId the doctor's ID
         * @param date     the date in YYYY-MM-DD format
         * @return list of available appointment slots
         */
        @GetMapping("/api/appointment-slots/available")
        public ResponseEntity<?> getAvailableSlots(
                        @RequestParam Long doctorId,
                        @RequestParam String date) {

                try {
                        // Validate date format
                        LocalDate parsedDate;
                        try {
                                parsedDate = LocalDate.parse(date, DateTimeFormatter.ISO_DATE);
                        } catch (DateTimeParseException e) {
                                return ResponseEntity.badRequest().body(
                                                Map.of("message", "Invalid date format. Expected format: YYYY-MM-DD"));
                        }

                        // Get doctor entity
                        DoctorProfile doctor = doctorProfileRepository.findById(doctorId)
                                        .orElse(null);

                        if (doctor == null) {
                                return ResponseEntity.badRequest().body(
                                                Map.of("message", "Doctor not found with ID: " + doctorId));
                        }

                        // Check if doctor has schedules for this date
                        List<DoctorSchedule> schedules = doctorScheduleRepository.findByDoctorAndScheduleDate(doctor,
                                        parsedDate);

                        if (schedules.isEmpty()) {
                                return ResponseEntity.ok()
                                                .body(Map.of(
                                                                "message", "Bác sĩ không có lịch làm việc vào ngày này",
                                                                "slots", Collections.emptyList()));
                        }

                        // Try direct SQL query for maximum transparency
                        String sql = "SELECT a.id, a.is_available, " +
                                        "t.start_time, t.end_time, " +
                                        "ds.schedule_date, " +
                                        "d.doctor_id " +
                                        "FROM appointment_slots a " +
                                        "JOIN time_slots t ON a.time_slot_id = t.id " +
                                        "JOIN doctor_schedule ds ON a.doctor_schedule_id = ds.id " +
                                        "JOIN doctor_profile d ON a.doctor_id = d.doctor_id " +
                                        "WHERE d.doctor_id = ? AND ds.schedule_date = ?";

                        List<Map<String, Object>> sqlResults = jdbcTemplate.queryForList(sql, doctorId, parsedDate);

                        if (!sqlResults.isEmpty()) {
                                List<Map<String, Object>> formattedResults = sqlResults.stream()
                                                .map(row -> {
                                                        Map<String, Object> formatted = new HashMap<>();
                                                        formatted.put("id", row.get("id"));
                                                        formatted.put("doctorId", row.get("doctor_id"));
                                                        formatted.put("startTime", row.get("start_time").toString());
                                                        formatted.put("endTime", row.get("end_time").toString());
                                                        formatted.put("isAvailable", row.get("is_available"));
                                                        formatted.put("scheduleDate",
                                                                        row.get("schedule_date").toString());
                                                        return formatted;
                                                })
                                                .collect(Collectors.toList());

                                return ResponseEntity.ok(formattedResults);
                        }

                        // Try direct repository access if SQL query failed
                        List<AppointmentSlot> directSlots = appointmentSlotRepository
                                        .findByDoctorAndDoctorSchedule_ScheduleDate(doctor, parsedDate);

                        // Map entities to DTOs
                        List<AppointmentSlotDTO> slots = directSlots.stream()
                                        .map(slot -> {
                                                return AppointmentSlotDTO.builder()
                                                                .id(slot.getId())
                                                                .doctorScheduleId(slot.getDoctorSchedule().getId())
                                                                .timeSlotId(slot.getTimeSlot().getId())
                                                                .doctorId(slot.getDoctor().getDoctorId())
                                                                .isAvailable(slot.getIsAvailable())
                                                                .timeSlotStart(slot.getTimeSlot().getStartTime()
                                                                                .toString())
                                                                .timeSlotEnd(slot.getTimeSlot().getEndTime().toString())
                                                                .scheduleDate(slot.getDoctorSchedule().getScheduleDate()
                                                                                .toString())
                                                                .build();
                                        })
                                        .collect(Collectors.toList());

                        // If direct repository access didn't work, try the service method
                        if (slots.isEmpty()) {
                                slots = appointmentSlotService.getSlotsByDoctorAndDate(doctorId, date);
                        }

                        // If we still have no slots, return an informative message
                        if (slots.isEmpty()) {
                                return ResponseEntity.ok()
                                                .body(Map.of(
                                                                "message",
                                                                "Không tìm thấy slot khám bệnh nào cho bác sĩ này vào ngày đã chọn",
                                                                "slots", Collections.emptyList()));
                        }

                        return ResponseEntity.ok(slots);
                } catch (Exception e) {
                        return ResponseEntity.badRequest().body(
                                        Map.of("message", "Error fetching appointment slots: " + e.getMessage()));
                }
        }

        /**
         * Alternative endpoint with different URL pattern for the same functionality
         */
        @GetMapping("/appointment-slots/doctor/{doctorId}/date/{date}")
        public ResponseEntity<?> getSlotsByDoctorAndDate(
                        @PathVariable Long doctorId,
                        @PathVariable String date) {

                return getAvailableSlots(doctorId, date);
        }

        /**
         * Get all appointment slots for debugging
         */
        @GetMapping("/api/appointment-slots/all")
        public ResponseEntity<?> getAllSlots() {
                try {
                        // Try direct SQL for maximum diagnostics
                        String sql = "SELECT a.id, a.is_available, " +
                                        "t.start_time, t.end_time, " +
                                        "ds.schedule_date, " +
                                        "d.doctor_id " +
                                        "FROM appointment_slots a " +
                                        "JOIN time_slots t ON a.time_slot_id = t.id " +
                                        "JOIN doctor_schedule ds ON a.doctor_schedule_id = ds.id " +
                                        "JOIN doctor_profile d ON a.doctor_id = d.doctor_id " +
                                        "LIMIT 100";

                        List<Map<String, Object>> sqlResults = jdbcTemplate.queryForList(sql);

                        if (!sqlResults.isEmpty()) {
                                List<Map<String, Object>> formattedResults = sqlResults.stream()
                                                .map(row -> {
                                                        Map<String, Object> formatted = new HashMap<>();
                                                        formatted.put("id", row.get("id"));
                                                        formatted.put("doctorId", row.get("doctor_id"));
                                                        formatted.put("startTime", row.get("start_time").toString());
                                                        formatted.put("endTime", row.get("end_time").toString());
                                                        formatted.put("isAvailable", row.get("is_available"));
                                                        formatted.put("scheduleDate",
                                                                        row.get("schedule_date").toString());
                                                        return formatted;
                                                })
                                                .collect(Collectors.toList());

                                return ResponseEntity.ok(formattedResults);
                        }

                        // Fallback to repository if SQL fails
                        List<AppointmentSlot> allSlots = appointmentSlotRepository.findAll();

                        // Return minimal info for debugging
                        List<Map<String, Object>> simplifiedSlots = allSlots.stream()
                                        .map(slot -> {
                                                Map<String, Object> info = new HashMap<>();
                                                info.put("id", slot.getId());
                                                info.put("doctorId", slot.getDoctor().getDoctorId());
                                                info.put("scheduleDate",
                                                                slot.getDoctorSchedule().getScheduleDate().toString());
                                                info.put("startTime", slot.getTimeSlot().getStartTime().toString());
                                                info.put("endTime", slot.getTimeSlot().getEndTime().toString());
                                                info.put("isAvailable", slot.getIsAvailable());
                                                return info;
                                        })
                                        .collect(Collectors.toList());

                        return ResponseEntity.ok(simplifiedSlots);
                } catch (Exception e) {
                        return ResponseEntity.badRequest().body(
                                        Map.of("message", "Error fetching all appointment slots: " + e.getMessage()));
                }
        }

        /**
         * Directly check the database tables for debugging
         * This endpoint is accessible without authentication
         */
        @GetMapping("/debug/database-tables")
        public ResponseEntity<?> debugDatabaseTables() {
                Map<String, Object> result = new HashMap<>();

                try {
                        // Check doctor_profile table
                        result.put("doctor_count", jdbcTemplate.queryForObject(
                                        "SELECT COUNT(*) FROM doctor_profile", Integer.class));

                        // Check doctor_schedule table
                        result.put("schedule_count", jdbcTemplate.queryForObject(
                                        "SELECT COUNT(*) FROM doctor_schedule", Integer.class));

                        // Check time_slots table
                        result.put("time_slot_count", jdbcTemplate.queryForObject(
                                        "SELECT COUNT(*) FROM time_slots", Integer.class));

                        // Check appointment_slots table
                        result.put("appointment_slot_count", jdbcTemplate.queryForObject(
                                        "SELECT COUNT(*) FROM appointment_slots", Integer.class));

                        // Sample data from each table - limit this to reduce response size
                        result.put("doctor_sample", jdbcTemplate.queryForList(
                                        "SELECT doctor_id, specialty FROM doctor_profile LIMIT 3"));

                        result.put("schedule_sample", jdbcTemplate.queryForList(
                                        "SELECT id, doctor_id, schedule_date FROM doctor_schedule LIMIT 3"));

                        result.put("time_slot_sample", jdbcTemplate.queryForList(
                                        "SELECT id, start_time, end_time FROM time_slots LIMIT 3"));

                        result.put("appointment_slot_sample", jdbcTemplate.queryForList(
                                        "SELECT id, doctor_id, is_available FROM appointment_slots LIMIT 3"));

                        return ResponseEntity.ok(result);
                } catch (Exception e) {
                        return ResponseEntity.badRequest().body(
                                        Map.of("message", "Error debugging database tables: " + e.getMessage()));
                }
        }

        /**
         * Get a specific appointment slot by ID
         */
        @GetMapping("/api/appointment-slots/{id}")
        public ResponseEntity<?> getSlotById(@PathVariable Long id) {
                try {
                        // This is a placeholder - you should implement this method in the service
                        // For now, we'll return an empty response
                        return ResponseEntity.ok(Collections.emptyMap());
                } catch (Exception e) {
                        return ResponseEntity.badRequest().body(
                                        Map.of("message", "Error fetching appointment slot: " + e.getMessage()));
                }
        }

        /**
         * Direct SQL query endpoint for appointment slots - no authentication required
         */
        @GetMapping("/debug/appointment-slots")
        public ResponseEntity<?> getAppointmentSlotsDebug(
                        @RequestParam(required = false) Long doctorId,
                        @RequestParam(required = false) String date) {

                try {
                        StringBuilder sql = new StringBuilder();
                        sql.append("SELECT a.id, a.is_available, ")
                                        .append("t.start_time, t.end_time, ")
                                        .append("ds.schedule_date, ")
                                        .append("d.doctor_id ")
                                        .append("FROM appointment_slots a ")
                                        .append("JOIN time_slots t ON a.time_slot_id = t.id ")
                                        .append("JOIN doctor_schedule ds ON a.doctor_schedule_id = ds.id ")
                                        .append("JOIN doctor_profile d ON a.doctor_id = d.doctor_id ");

                        List<Object> params = new ArrayList<>();

                        // Add conditions if parameters are provided
                        if (doctorId != null && date != null) {
                                sql.append("WHERE d.doctor_id = ? AND ds.schedule_date = ? ");
                                params.add(doctorId);
                                params.add(date);
                        } else if (doctorId != null) {
                                sql.append("WHERE d.doctor_id = ? ");
                                params.add(doctorId);
                        } else if (date != null) {
                                sql.append("WHERE ds.schedule_date = ? ");
                                params.add(date);
                        }

                        // Add limit to avoid too many results
                        sql.append("LIMIT 100");

                        List<Map<String, Object>> sqlResults = jdbcTemplate.queryForList(
                                        sql.toString(),
                                        params.toArray());

                        if (!sqlResults.isEmpty()) {
                                List<Map<String, Object>> formattedResults = sqlResults.stream()
                                                .map(row -> {
                                                        Map<String, Object> formatted = new HashMap<>();
                                                        formatted.put("id", row.get("id"));
                                                        formatted.put("doctorId", row.get("doctor_id"));
                                                        formatted.put("startTime", row.get("start_time").toString());
                                                        formatted.put("endTime", row.get("end_time").toString());
                                                        formatted.put("isBooked",
                                                                        !Boolean.TRUE.equals(row.get("is_available")));
                                                        formatted.put("isAvailable", row.get("is_available"));
                                                        formatted.put("scheduleDate",
                                                                        row.get("schedule_date").toString());
                                                        return formatted;
                                                })
                                                .collect(Collectors.toList());

                                return ResponseEntity.ok(formattedResults);
                        }

                        // If no results, return informative message
                        return ResponseEntity.ok()
                                        .body(Map.of(
                                                        "message", "Không tìm thấy slot khám bệnh nào",
                                                        "slots", Collections.emptyList()));
                } catch (Exception e) {
                        return ResponseEntity.badRequest().body(
                                        Map.of("message", "Error in direct SQL query: " + e.getMessage()));
                }
        }

        /**
         * Alternative endpoint with /api prefix for appointment slots - no
         * authentication required
         */
        @GetMapping("/api/debug/appointment-slots")
        public ResponseEntity<?> getAppointmentSlotsDebugWithApiPrefix(
                        @RequestParam(required = false) Long doctorId,
                        @RequestParam(required = false) String date) {
                return getAppointmentSlotsDebug(doctorId, date);
        }

        /**
         * Alternative endpoint with /api prefix for database tables - no authentication
         * required
         */
        @GetMapping("/api/debug/database-tables")
        public ResponseEntity<?> debugDatabaseTablesWithApiPrefix() {
                return debugDatabaseTables();
        }

        /**
         * Directly insert an appointment record for debugging purposes
         * This provides a workaround when the JPA entity mapping has issues
         */
        @PostMapping("/debug/direct-appointment-insert")
        public ResponseEntity<?> directAppointmentInsert(@RequestBody Map<String, Object> appointmentData) {
                Map<String, Object> result = new HashMap<>();

                try {
                        // Extract values from request
                        Long patientId = Long.valueOf(appointmentData.get("patientId").toString());
                        Long doctorId = Long.valueOf(appointmentData.get("doctorId").toString());
                        Long appointmentSlotId = Long.valueOf(appointmentData.get("appointmentSlotId").toString());
                        String appointmentType = (String) appointmentData.get("appointmentType");
                        String symptoms = appointmentData.get("symptoms") != null
                                        ? (String) appointmentData.get("symptoms")
                                        : "";
                        String notes = appointmentData.get("notes") != null ? (String) appointmentData.get("notes")
                                        : "";
                        int isAnonymous = Integer.valueOf(appointmentData.get("isAnonymous").toString());

                        // Handle timestamp carefully
                        String created_at = null;
                        if (appointmentData.get("created_at") != null) {
                                created_at = appointmentData.get("created_at").toString();
                                // Make sure MySQL DATETIME(6) format is correct: YYYY-MM-DD HH:MM:SS.ssssss
                                if (!created_at.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}(\\.\\d{1,6})?")) {
                                        created_at = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
                                                        .format(new java.util.Date());
                                }
                        } else {
                                // Use current timestamp if not provided
                                created_at = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
                                                .format(new java.util.Date());
                        }

                        // First, check and mark slot as unavailable
                        String updateSlotSql = "UPDATE appointment_slots SET is_available = false WHERE id = ?";
                        int slotUpdated = jdbcTemplate.update(updateSlotSql, appointmentSlotId);

                        if (slotUpdated == 0) {
                                throw new Exception("Failed to mark appointment slot as unavailable");
                        }

                        // Insert appointment record directly into database with explicit created_at
                        String sql = "INSERT INTO appointment (patient_id, doctor_id, appointment_slot_id, appointment_type, "
                                        +
                                        "status, is_anonymous, symptoms, notes, created_at) " +
                                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

                        int rowsInserted = jdbcTemplate.update(
                                        sql,
                                        patientId,
                                        doctorId,
                                        appointmentSlotId,
                                        appointmentType,
                                        "Chờ xác nhận",
                                        isAnonymous,
                                        symptoms,
                                        notes,
                                        created_at);

                        // Get the last inserted ID
                        Long newAppointmentId = jdbcTemplate.queryForObject(
                                        "SELECT LAST_INSERT_ID()", Long.class);

                        // Fetch the inserted record for confirmation
                        String selectSql = "SELECT * FROM appointment WHERE id = ?";
                        Map<String, Object> insertedRecord = jdbcTemplate.queryForMap(selectSql, newAppointmentId);

                        result.put("success", true);
                        result.put("message", "Appointment record inserted directly via JDBC");
                        result.put("rowsInserted", rowsInserted);
                        result.put("appointmentId", newAppointmentId);
                        result.put("record", insertedRecord);

                        return ResponseEntity.ok(result);
                } catch (Exception e) {
                        result.put("success", false);
                        result.put("error", e.getMessage());
                        result.put("errorType", e.getClass().getName());

                        // Add detailed error tracing
                        StringBuilder stack = new StringBuilder();
                        for (StackTraceElement element : e.getStackTrace()) {
                                stack.append(element.toString()).append("\n");
                        }
                        result.put("stackTrace", stack.toString());

                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
                }
        }

        /**
         * Debug endpoint to check recent appointments
         */
        @GetMapping("/debug/recent-appointments")
        public ResponseEntity<?> getRecentAppointments() {
                try {
                        String sql = "SELECT a.*, " +
                                        "u.full_name as patient_name, " +
                                        "d.user_id, " +
                                        "du.full_name as doctor_name " +
                                        "FROM appointment a " +
                                        "LEFT JOIN users u ON a.patient_id = u.id " +
                                        "JOIN doctor_profile d ON a.doctor_id = d.doctor_id " +
                                        "JOIN users du ON d.user_id = du.id " +
                                        "ORDER BY a.created_at DESC " +
                                        "LIMIT 20";

                        List<Map<String, Object>> appointments = jdbcTemplate.queryForList(sql);

                        // Include column metadata for debugging
                        if (!appointments.isEmpty()) {
                                String metadataSql = "SHOW COLUMNS FROM appointment";
                                List<Map<String, Object>> columnMetadata = jdbcTemplate.queryForList(metadataSql);

                                Map<String, Object> response = new HashMap<>();
                                response.put("appointments", appointments);
                                response.put("columns", columnMetadata);

                                return ResponseEntity.ok(appointments);
                        }

                        return ResponseEntity.ok(appointments);
                } catch (Exception e) {
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(Map.of(
                                                        "error", "Failed to fetch recent appointments",
                                                        "message", e.getMessage()));
                }
        }
}