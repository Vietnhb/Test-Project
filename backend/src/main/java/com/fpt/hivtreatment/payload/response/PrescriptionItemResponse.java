package com.fpt.hivtreatment.payload.response;

import lombok.Data;
import lombok.Builder;

@Data
@Builder
public class PrescriptionItemResponse {
    
    private Long id;
    private Long prescriptionId;
    private Long patientId;
    private Long medicationId;
    private Double morningDose;
    private Double noonDose;
    private Double afternoonDose;
    private Double eveningDose;
    private Double dailyTotal;
    private String instructions;
    private String status;
    
    // Related entities
    private String medicationName;
    private String medicationUnit;
}
