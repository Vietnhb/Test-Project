package com.fpt.hivtreatment.service.impl;

import com.fpt.hivtreatment.exception.ResourceNotFoundException;
import com.fpt.hivtreatment.model.entity.Appointment;
import com.fpt.hivtreatment.model.entity.AppointmentSlot;
import com.fpt.hivtreatment.model.entity.DoctorProfile;
import com.fpt.hivtreatment.model.entity.User;
import com.fpt.hivtreatment.payload.request.AppointmentRequest;
import com.fpt.hivtreatment.payload.response.AppointmentResponse;
import com.fpt.hivtreatment.repository.AppointmentRepository;
import com.fpt.hivtreatment.repository.AppointmentSlotRepository;
import com.fpt.hivtreatment.repository.DoctorProfileRepository;
import com.fpt.hivtreatment.repository.UserRepository;
import com.fpt.hivtreatment.service.AppointmentService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AppointmentServiceImpl implements AppointmentService {

        private static final Logger logger = LoggerFactory.getLogger(AppointmentServiceImpl.class);

        private final AppointmentRepository appointmentRepository;
        private final AppointmentSlotRepository appointmentSlotRepository;
        private final UserRepository userRepository;
        private final DoctorProfileRepository doctorProfileRepository;

        @Override
        @Transactional
        public AppointmentResponse createAppointment(AppointmentRequest request) {
                logger.info("Creating appointment with slot ID: {}", request.getAppointmentSlotId());

                // Log the appointment date from request if available
                if (request.getAppointmentDate() != null && !request.getAppointmentDate().isEmpty()) {
                        logger.info("Appointment date from request: {}", request.getAppointmentDate());
                }

                // Find the appointment slot
                AppointmentSlot slot = appointmentSlotRepository.findById(request.getAppointmentSlotId())
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Appointment slot not found with id: "
                                                                + request.getAppointmentSlotId()));

                // Check if the slot is available
                if (!slot.getIsAvailable()) {
                        logger.error("Appointment slot is already booked: {}", request.getAppointmentSlotId());
                        throw new IllegalStateException("This appointment slot is already booked");
                }

<<<<<<< HEAD
                // Find the patient - always get patient regardless of anonymous status
                User patient = userRepository.findById(request.getPatientId())
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Patient not found with id: " + request.getPatientId()));
=======
                // Find the patient
                User patient = null;
                if (request.getPatientId() != null && !request.getIsAnonymous()) {
                        patient = userRepository.findById(request.getPatientId())
                                        .orElseThrow(() -> new ResourceNotFoundException(
                                                        "Patient not found with id: " + request.getPatientId()));
                }
>>>>>>> fd42c148e0431975301ca683137e9cc7dea64a1c

                // Find the doctor
                DoctorProfile doctor = doctorProfileRepository.findById(request.getDoctorId())
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Doctor not found with id: " + request.getDoctorId()));

                // Create the appointment
                Appointment appointment = Appointment.builder()
                                .patient(patient)
                                .doctor(doctor)
                                .appointmentSlot(slot)
                                .appointmentType(request.getAppointmentType())
                                .isAnonymous(request.getIsAnonymous())
                                .symptoms(request.getSymptoms())
                                .notes(request.getNotes())
                                .status("Chờ xác nhận")
                                .build();

                // Save the appointment
                Appointment savedAppointment = appointmentRepository.save(appointment);
                logger.info("Appointment created with ID: {}", savedAppointment.getId());

                // Mark the slot as unavailable
                slot.setIsAvailable(false);
                appointmentSlotRepository.save(slot);
                logger.info("Appointment slot {} marked as unavailable", slot.getId());

                // Return response
                return mapToResponse(savedAppointment);
        }

        @Override
        @Transactional
        public AppointmentResponse updateStatus(Long id, String status) {
                logger.info("Updating appointment status with ID: {} to {}", id, status);

                Appointment appointment = appointmentRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Appointment not found with id: " + id));

                appointment.setStatus(status);

                // If cancelled, free up the slot
                if ("Đã hủy".equals(status)) {
                        appointment.getAppointmentSlot().setIsAvailable(true);
                        appointmentSlotRepository.save(appointment.getAppointmentSlot());
                        logger.info("Appointment slot {} marked as available after cancellation",
                                        appointment.getAppointmentSlot().getId());
                }

                Appointment updatedAppointment = appointmentRepository.save(appointment);
                return mapToResponse(updatedAppointment);
        }

        @Override
        public List<AppointmentResponse> getAppointmentsByPatient(Long patientId) {
                User patient = userRepository.findById(patientId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Patient not found with id: " + patientId));

                return appointmentRepository.findByPatient(patient)
                                .stream()
                                .map(this::mapToResponse)
                                .collect(Collectors.toList());
        }

        @Override
        public List<AppointmentResponse> getAppointmentsByDoctor(Long doctorId) {
                DoctorProfile doctor = doctorProfileRepository.findById(doctorId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Doctor not found with id: " + doctorId));

                return appointmentRepository.findByDoctor(doctor)
                                .stream()
                                .map(this::mapToResponse)
                                .collect(Collectors.toList());
        }

        @Override
        public List<AppointmentResponse> getAppointmentsByPatientAndStatus(Long patientId, String status) {
                User patient = userRepository.findById(patientId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Patient not found with id: " + patientId));

                return appointmentRepository.findByPatientAndStatus(patient, status)
                                .stream()
                                .map(this::mapToResponse)
                                .collect(Collectors.toList());
        }

        @Override
        public List<AppointmentResponse> getAppointmentsByDoctorAndStatus(Long doctorId, String status) {
                DoctorProfile doctor = doctorProfileRepository.findById(doctorId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Doctor not found with id: " + doctorId));

                return appointmentRepository.findByDoctorAndStatus(doctor, status)
                                .stream()
                                .map(this::mapToResponse)
                                .collect(Collectors.toList());
        }

        @Override
        public AppointmentResponse getAppointmentById(Long id) {
                Appointment appointment = appointmentRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Appointment not found with id: " + id));

                return mapToResponse(appointment);
        }

        @Override
        public long countAppointmentsByStatus(String status) {
                logger.info("Counting appointments with status: {}", status);
                return appointmentRepository.countByStatus(status);
        }

        @Override
        public Map<String, Object> getAppointmentsForManager(String status, String startDate, String endDate, int page,
                        int size, int limit) {
                logger.info("Getting appointments for manager with status: {}, startDate: {}, endDate: {}, page: {}, size: {}, limit: {}",
                                status, startDate, endDate, page, size, limit);

                Map<String, Object> result = new HashMap<>();
                List<Appointment> appointments = new ArrayList<>();

                try {
                        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
                        Page<Appointment> pageAppointments;

                        // Filter by status if provided
                        if (status != null && !status.isEmpty()) {
                                // If date range is provided
                                if (startDate != null && !startDate.isEmpty() && endDate != null
                                                && !endDate.isEmpty()) {
                                        try {
                                                LocalDate start = LocalDate.parse(startDate,
                                                                DateTimeFormatter.ISO_DATE);
                                                LocalDate end = LocalDate.parse(endDate, DateTimeFormatter.ISO_DATE);
                                                // Implement filtering by status and date range (this would require a
                                                // custom repository method)
                                                pageAppointments = appointmentRepository
                                                                .findByStatusAndDateRange(status, start, end, pageable);
                                        } catch (DateTimeParseException e) {
                                                logger.error("Invalid date format: {}, {}", startDate, endDate, e);
                                                pageAppointments = appointmentRepository.findByStatus(status, pageable);
                                        }
                                } else {
                                        // Only filter by status
                                        pageAppointments = appointmentRepository.findByStatus(status, pageable);
                                }
                        } else {
                                // Get all appointments
                                pageAppointments = appointmentRepository.findAll(pageable);
                        }

                        appointments = pageAppointments.getContent();

                        // Apply limit if specified and less than fetched size
                        if (limit > 0 && limit < appointments.size()) {
                                appointments = appointments.subList(0, limit);
                        }

                        // Map appointments to response
                        List<AppointmentResponse> appointmentResponses = appointments.stream()
                                        .map(this::mapToResponse)
                                        .collect(Collectors.toList());

                        // Build result
                        result.put("appointments", appointmentResponses);
                        result.put("currentPage", page);
                        result.put("totalItems", pageAppointments.getTotalElements());
                        result.put("totalPages", pageAppointments.getTotalPages());

                        return result;
                } catch (Exception e) {
                        logger.error("Error getting appointments for manager", e);
                        throw e;
                }
        }

        @Override
        @Transactional
        public Map<String, Object> updateAppointmentStatus(Long id, String status, String cancellationReason) {
                logger.info("Updating appointment status with ID: {} to {}, reason: {}", id, status,
                                cancellationReason);

                Map<String, Object> result = new HashMap<>();

                Appointment appointment = appointmentRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Appointment not found with id: " + id));

                appointment.setStatus(status);

                // Set cancellation reason if provided and status is cancelled
                if ("Đã hủy".equals(status) && cancellationReason != null && !cancellationReason.isEmpty()) {
                        appointment.setCancellationReason(cancellationReason);
                        logger.info("Setting cancellation reason: {} for appointment: {}", cancellationReason, id);
                }

                // If cancelled, free up the slot
                if ("Đã hủy".equals(status)) {
                        appointment.getAppointmentSlot().setIsAvailable(true);
                        appointmentSlotRepository.save(appointment.getAppointmentSlot());
                        logger.info("Appointment slot {} marked as available after cancellation",
                                        appointment.getAppointmentSlot().getId());
                }

                Appointment updatedAppointment = appointmentRepository.save(appointment);
                result.put("appointment", mapToResponse(updatedAppointment));
                result.put("success", true);
                result.put("message", "Appointment status updated successfully");

                return result;
        }

        private AppointmentResponse mapToResponse(Appointment appointment) {
                // Use safe formatting approach
                String appointmentDate = "";
                try {
                        if (appointment.getAppointmentSlot() != null &&
                                        appointment.getAppointmentSlot().getDoctorSchedule() != null &&
                                        appointment.getAppointmentSlot().getDoctorSchedule()
                                                        .getScheduleDate() != null) {

                                // Check the type of scheduleDate and handle accordingly
                                Object dateObj = appointment.getAppointmentSlot().getDoctorSchedule().getScheduleDate();
                                if (dateObj instanceof java.util.Date) {
                                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                                        appointmentDate = dateFormat.format(dateObj);
                                } else if (dateObj instanceof java.time.LocalDate) {
                                        appointmentDate = dateObj.toString();
                                } else if (dateObj instanceof String) {
                                        appointmentDate = (String) dateObj;
                                } else {
                                        logger.warn("Unknown date type: {}", dateObj.getClass().getName());
                                        appointmentDate = dateObj.toString();
                                }
                        }
                } catch (Exception e) {
                        logger.error("Error formatting appointment date", e);
                        appointmentDate = "Unknown date";
                }

                String patientName = appointment.getPatient() != null ? appointment.getPatient().getFullName()
                                : "Ẩn danh";

                // Safe time formatting
                String startTime = "";
                String endTime = "";
                try {
                        if (appointment.getAppointmentSlot() != null &&
                                        appointment.getAppointmentSlot().getTimeSlot() != null) {
                                startTime = appointment.getAppointmentSlot().getTimeSlot().getStartTime().toString();
                                endTime = appointment.getAppointmentSlot().getTimeSlot().getEndTime().toString();
                        }
                } catch (Exception e) {
                        logger.error("Error formatting appointment time", e);
                }

                return AppointmentResponse.builder()
                                .id(appointment.getId())
                                .patientId(appointment.getPatient() != null ? appointment.getPatient().getId() : null)
                                .patientName(patientName)
                                .doctorId(appointment.getDoctor().getDoctorId())
                                .doctorName(appointment.getDoctor().getUser().getFullName())
                                .appointmentSlotId(appointment.getAppointmentSlot().getId())
                                .appointmentType(appointment.getAppointmentType())
                                .status(appointment.getStatus())
                                .isAnonymous(appointment.getIsAnonymous())
                                .symptoms(appointment.getSymptoms())
                                .notes(appointment.getNotes())
                                .cancellationReason(appointment.getCancellationReason())
                                .createdAt(appointment.getCreatedAt())
                                .appointmentDate(appointmentDate)
                                .startTime(startTime)
                                .endTime(endTime)
                                .build();
        }
}