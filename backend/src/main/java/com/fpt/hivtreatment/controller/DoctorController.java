package com.fpt.hivtreatment.controller;

import com.fpt.hivtreatment.model.entity.DoctorProfile;
import com.fpt.hivtreatment.model.entity.User;
import com.fpt.hivtreatment.repository.DoctorProfileRepository;
import com.fpt.hivtreatment.dto.DoctorDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller xử lý các yêu cầu API liên quan đến bác sĩ
 */
@RestController
@RequestMapping("/api/doctors")
public class DoctorController {

    private static final Logger logger = LoggerFactory.getLogger(DoctorController.class);

    @Autowired
    private DoctorProfileRepository doctorProfileRepository;

    /**
     * API lấy tất cả thông tin bác sĩ
     */
    @GetMapping
    public ResponseEntity<List<DoctorDTO>> getAllDoctors() {
        logger.info("Nhận yêu cầu lấy danh sách tất cả bác sĩ");

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
    public ResponseEntity<DoctorDTO> getDoctorById(@PathVariable Long doctorId) {
        logger.info("Nhận yêu cầu lấy thông tin bác sĩ có id: {}", doctorId);

        return doctorProfileRepository.findById(doctorId)
                .map(this::convertToDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * API lấy danh sách bác sĩ theo chuyên khoa
     */
    @GetMapping("/specialty/{specialty}")
    public ResponseEntity<List<DoctorDTO>> getDoctorsBySpecialty(@PathVariable String specialty) {
        logger.info("Nhận yêu cầu lấy danh sách bác sĩ theo chuyên khoa: {}", specialty);

        List<DoctorProfile> doctors = doctorProfileRepository.findBySpecialtyIgnoreCase(specialty);
        List<DoctorDTO> doctorDTOs = doctors.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(doctorDTOs);
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
}