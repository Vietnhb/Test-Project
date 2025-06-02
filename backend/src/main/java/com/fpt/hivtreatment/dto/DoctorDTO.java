package com.fpt.hivtreatment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO cho thông tin bác sĩ
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DoctorDTO {
    private Long id;
    private Long userId;
    private String fullName;
    private String email;
    private String phone;
    private String specialty;
    private String qualification;
    private Integer experience;
    private String bio;
    private Boolean isActive;
}