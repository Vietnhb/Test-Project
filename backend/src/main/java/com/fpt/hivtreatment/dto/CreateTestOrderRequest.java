package com.fpt.hivtreatment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTestOrderRequest {
    private Long testTypeId;
    private Long patientId;
    private Long doctorId;
    private Long medicalRecordId;
    private String notes;
}