package com.fpt.hivtreatment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LabTestOrderDTO {
    private Long id;
    private Long testTypeId;
    private String testTypeName;
    private Long patientId;
    private String patientName;
    private Long doctorId;
    private String doctorName;
    private Long medicalRecordId;
    private Long staffId;
    private String staffName;
    private LocalDate orderDate;
    private LocalDateTime resultExpectedDate;
    private String status;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime sampleCollectedAt;
    private LocalDateTime resultUpdatedAt;
    private String result;
    private String resultNotes;
}