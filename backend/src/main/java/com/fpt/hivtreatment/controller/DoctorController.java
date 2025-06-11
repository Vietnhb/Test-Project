package com.fpt.hivtreatment.controller;

import com.fpt.hivtreatment.model.entity.DoctorProfile;
import com.fpt.hivtreatment.model.entity.User;
import com.fpt.hivtreatment.repository.DoctorProfileRepository;
import com.fpt.hivtreatment.dto.DoctorDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.time.LocalDate;

import com.fpt.hivtreatment.repository.AppointmentRepository;
import com.fpt.hivtreatment.model.entity.Appointment;
import com.fpt.hivtreatment.dto.AppointmentDTO;

/**
 * Controller xử lý các yêu cầu API liên quan đến bác sĩ
 */
@RestController
@RequestMapping("/api/doctors")
public class DoctorController {

    @Autowired
    private DoctorProfileRepository doctorProfileRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    /**
     * Simple API test endpoint that doesn't require authentication
     */
    @GetMapping("/test")
    public ResponseEntity<?> testEndpoint() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Doctor API is working");
        return ResponseEntity.ok(response);
    }

    /**
     * API lấy tất cả thông tin bác sĩ
     */
    @GetMapping
    public ResponseEntity<List<DoctorDTO>> getAllDoctors() {
        List<DoctorProfile> doctors = doctorProfileRepository.findAll();

        List<DoctorDTO> doctorDTOs = doctors.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(doctorDTOs);
    }

    /**
     * API lấy thông tin bác sĩ theo ID
     */
    @GetMapping("/{doctorId}")
    public ResponseEntity<?> getDoctorById(@PathVariable Long doctorId) {
        Optional<DoctorProfile> doctorOpt = doctorProfileRepository.findById(doctorId);

        if (doctorOpt.isPresent()) {
            DoctorDTO dto = convertToDTO(doctorOpt.get());
            return ResponseEntity.ok(dto);
        } else {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Không tìm thấy bác sĩ");
            response.put("doctorId", doctorId.toString());
            return ResponseEntity.status(404).body(response);
        }
    }

    /**
     * API lấy thông tin bác sĩ theo USER ID
     */
    @GetMapping("/by-user/{userId}")
    public ResponseEntity<?> getDoctorByUserId(@PathVariable Long userId) {
        Optional<DoctorProfile> doctorOpt = doctorProfileRepository.findByUser_Id(userId);

        if (doctorOpt.isPresent()) {
            DoctorDTO dto = convertToDTO(doctorOpt.get());
            return ResponseEntity.ok(dto);
        } else {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Không tìm thấy bác sĩ với user ID này");
            response.put("userId", userId.toString());
            return ResponseEntity.status(404).body(response);
        }
    }

    /**
     * API lấy danh sách bác sĩ theo chuyên khoa
     */
    @GetMapping("/specialty/{specialty}")
    public ResponseEntity<List<DoctorDTO>> getDoctorsBySpecialty(@PathVariable String specialty) {
        List<DoctorProfile> doctors = doctorProfileRepository.findBySpecialtyIgnoreCase(specialty);
        List<DoctorDTO> doctorDTOs = doctors.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(doctorDTOs);
    }

    /**
     * API lấy danh sách lịch hẹn đã xác nhận của bác sĩ trong ngày hôm nay
     */
    @GetMapping("/{doctorId}/appointments/today")
    public ResponseEntity<?> getTodayAppointments(@PathVariable Long doctorId) {
        try {
            // Kiểm tra bác sĩ có tồn tại không
            Optional<DoctorProfile> doctorOpt = doctorProfileRepository.findById(doctorId);
            if (!doctorOpt.isPresent()) {
                Map<String, String> response = new HashMap<>();
                response.put("error", "Không tìm thấy bác sĩ");
                return ResponseEntity.status(404).body(response);
            }

            // Lấy ngày hôm nay
            LocalDate today = LocalDate.now();

            // Chỉ lấy các lịch hẹn có trạng thái "Đã xác nhận"
            String confirmedStatus = "Đã xác nhận";
            List<Appointment> appointments = appointmentRepository.findConfirmedAppointmentsForDoctorByDate(
                    doctorId, confirmedStatus, today);

            // Chuyển thành DTO và trả về
            List<AppointmentDTO> appointmentDTOs = appointments.stream()
                    .map(this::convertToAppointmentDTO)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(appointmentDTOs);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Lỗi khi lấy danh sách lịch hẹn");
            response.put("message", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * API lấy danh sách lịch tư vấn đã xác nhận của bác sĩ trong ngày hôm nay
     */
    @GetMapping("/{doctorId}/consultations/today")
    public ResponseEntity<?> getTodayConsultations(@PathVariable Long doctorId) {
        try {
            // Kiểm tra bác sĩ có tồn tại không
            Optional<DoctorProfile> doctorOpt = doctorProfileRepository.findById(doctorId);
            if (!doctorOpt.isPresent()) {
                Map<String, String> response = new HashMap<>();
                response.put("error", "Không tìm thấy bác sĩ");
                return ResponseEntity.status(404).body(response);
            }

            // Lấy ngày hôm nay
            LocalDate today = LocalDate.now();

            // Chỉ lấy các lịch tư vấn có trạng thái "Đã xác nhận"
            String confirmedStatus = "Đã xác nhận";
            String appointmentType = "Tư vấn";
            List<Appointment> consultations = appointmentRepository.findAppointmentsByTypeAndStatusForDoctorByDate(
                    doctorId, appointmentType, confirmedStatus, today);

            // Chuyển thành DTO và trả về
            List<AppointmentDTO> consultationDTOs = consultations.stream()
                    .map(this::convertToAppointmentDTO)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(consultationDTOs);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Lỗi khi lấy danh sách lịch tư vấn");
            response.put("message", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * API lấy danh sách lịch khám bệnh (không phải tư vấn) đã xác nhận của bác sĩ
     * trong ngày hôm nay
     */
    @GetMapping("/{doctorId}/medical-appointments/today")
    public ResponseEntity<?> getTodayMedicalAppointments(@PathVariable Long doctorId) {
        try {
            // Kiểm tra bác sĩ có tồn tại không
            Optional<DoctorProfile> doctorOpt = doctorProfileRepository.findById(doctorId);
            if (!doctorOpt.isPresent()) {
                Map<String, String> response = new HashMap<>();
                response.put("error", "Không tìm thấy bác sĩ");
                return ResponseEntity.status(404).body(response);
            }

            // Lấy ngày hôm nay
            LocalDate today = LocalDate.now();

            // Chỉ lấy các lịch khám bệnh có trạng thái "Đã xác nhận" và không phải loại "Tư
            // vấn"
            String confirmedStatus = "Đã xác nhận";
            String appointmentType = "Tư vấn";
            List<Appointment> appointments = appointmentRepository.findAppointmentsExcludeTypeAndStatusForDoctorByDate(
                    doctorId, appointmentType, confirmedStatus, today);

            // Chuyển thành DTO và trả về
            List<AppointmentDTO> appointmentDTOs = appointments.stream()
                    .map(this::convertToAppointmentDTO)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(appointmentDTOs);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Lỗi khi lấy danh sách lịch khám bệnh");
            response.put("message", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Phương thức chuyển đổi entity thành DTO
     */
    private DoctorDTO convertToDTO(DoctorProfile doctor) {
        User user = doctor.getUser();
        return DoctorDTO.builder()
                .id(doctor.getDoctorId())
                .userId(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .specialty(doctor.getSpecialty())
                .qualification(doctor.getQualification())
                .experience(doctor.getExperience())
                .bio(doctor.getBio())
                .phone(user.getPhoneNumber())
                .isActive(true) // Default to true since we removed this field
                .build();
    }

    /**
     * Phương thức chuyển đổi Appointment thành AppointmentDTO
     */
    private AppointmentDTO convertToAppointmentDTO(Appointment appointment) {
        User patient = appointment.getPatient();
        return AppointmentDTO.builder()
                .id(appointment.getId())
                .patientId(patient.getId())
                .patientName(patient.getFullName())
                .appointmentType(appointment.getAppointmentType())
                .status(appointment.getStatus())
                .symptoms(appointment.getSymptoms())
                .notes(appointment.getNotes())
                .isAnonymous(appointment.getIsAnonymous())
                .timeSlot(appointment.getAppointmentSlot().getTimeSlot().getStartTime() + " - " +
                        appointment.getAppointmentSlot().getTimeSlot().getEndTime())
                .date(appointment.getAppointmentSlot().getDoctorSchedule().getScheduleDate())
                .build();
    }
}