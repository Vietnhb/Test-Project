package com.fpt.hivtreatment.controller;

import com.fpt.hivtreatment.payload.request.AppointmentRequest;
import com.fpt.hivtreatment.payload.response.AppointmentResponse;
import com.fpt.hivtreatment.service.AppointmentService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AppointmentController {

    private static final Logger logger = LoggerFactory.getLogger(AppointmentController.class);
    private final AppointmentService appointmentService;

    /**
     * Endpoint to create a new appointment
     * This will also mark the slot as unavailable
     * 
     * @param request The appointment data
     * @return The created appointment
     */
    @PostMapping("/appointments")
    @PreAuthorize("hasAuthority('1') or hasAuthority('5')") // Allow patients (role 1) and managers (role 5)
    public ResponseEntity<?> createAppointment(@Valid @RequestBody AppointmentRequest request) {
        logger.info("Received request to create appointment for slot ID: {}", request.getAppointmentSlotId());

        try {
            AppointmentResponse createdAppointment = appointmentService.createAppointment(request);

            logger.info("Appointment created successfully with ID: {}", createdAppointment.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(createdAppointment);
        } catch (IllegalStateException e) {
            // Slot already booked
            logger.error("Failed to create appointment: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error creating appointment", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error creating appointment: " + e.getMessage()));
        }
    }

    /**
     * Update appointment status
     * 
     * @param id     Appointment ID
     * @param status New status
     * @return Updated appointment
     */
    @PutMapping("/appointments/{id}/status")
    @PreAuthorize("hasAnyAuthority('1', '2', '5')") // Allow patients, doctors and managers
    public ResponseEntity<?> updateAppointmentStatus(
            @PathVariable Long id,
            @RequestParam String status) {

        logger.info("Received request to update appointment status for ID: {} to {}", id, status);

        try {
            AppointmentResponse updatedAppointment = appointmentService.updateStatus(id, status);
            logger.info("Appointment status updated successfully for ID: {}", id);
            return ResponseEntity.ok(updatedAppointment);
        } catch (Exception e) {
            logger.error("Error updating appointment status", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error updating appointment status: " + e.getMessage()));
        }
    }

    /**
     * Get appointments by patient
     * 
     * @param patientId Patient ID
     * @return List of appointments
     */
    @GetMapping("/patient/appointments")
    @PreAuthorize("hasAuthority('1')") // Only patients
    public ResponseEntity<?> getPatientAppointments(
            @RequestParam Long patientId) {

        logger.info("Fetching appointments for patient ID: {}", patientId);

        try {
            List<AppointmentResponse> appointments = appointmentService.getAppointmentsByPatient(patientId);
            logger.info("Found {} appointments for patient ID: {}", appointments.size(), patientId);

            Map<String, Object> response = new HashMap<>();
            response.put("appointments", appointments);
            response.put("totalItems", appointments.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error fetching patient appointments", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error fetching appointments: " + e.getMessage()));
        }
    }

    /**
     * Get appointments by doctor
     * 
     * @param doctorId Doctor ID
     * @return List of appointments
     */
    @GetMapping("/doctor/appointments")
    @PreAuthorize("hasAuthority('2')") // Doctor role
    public ResponseEntity<?> getDoctorAppointments(
            @RequestParam Long doctorId) {

        logger.info("Fetching appointments for doctor ID: {}", doctorId);

        try {
            List<AppointmentResponse> appointments = appointmentService.getAppointmentsByDoctor(doctorId);
            logger.info("Found {} appointments for doctor ID: {}", appointments.size(), doctorId);

            Map<String, Object> response = new HashMap<>();
            response.put("appointments", appointments);
            response.put("totalItems", appointments.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error fetching doctor appointments", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error fetching appointments: " + e.getMessage()));
        }
    }

    /**
     * Get appointment details
     * 
     * @param id Appointment ID
     * @return Appointment details
     */
    @GetMapping({ "/appointments/{id}", "/api/appointments/{id}" })
    @PreAuthorize("hasAuthority('1') or hasAuthority('2') or hasAuthority('3') or hasAuthority('4') or hasAuthority('5')") // Tất
                                                                                                                           // cả
                                                                                                                           // người
                                                                                                                           // dùng
                                                                                                                           // đã
                                                                                                                           // xác
                                                                                                                           // thực
    public ResponseEntity<?> getAppointmentDetails(@PathVariable Long id) {
        logger.info("Fetching appointment details for ID: {}", id);

        try {
            AppointmentResponse appointment = appointmentService.getAppointmentById(id);
            return ResponseEntity.ok(appointment);
        } catch (Exception e) {
            logger.error("Error fetching appointment details", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error fetching appointment details: " + e.getMessage()));
        }
    }

    /**
     * Test endpoint - no authentication required
     */
    @GetMapping("/appointments/test")
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> testEndpoint() {
        return ResponseEntity.ok(Map.of("message", "Appointment API is working"));
    }
}