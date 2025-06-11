package com.fpt.hivtreatment.payload.response;

import lombok.Data;
import lombok.Builder;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class PrescriptionResponse {
    
    private Long id;
    private Long patientId;
    private Long medicalRecordId;
    private Long doctorId;
    private Long protocolId;
    private LocalDate prescriptionDate;
    private LocalDate startDate;
    private LocalDate endDate;
    private String doctorNotes;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Related entities
    private String patientName;
    private String doctorName;
    private String protocolName;
    
    // Prescription items
    private List<PrescriptionItemResponse> prescriptionItems;
    
    // Summary information
    private Integer totalMedications;
    private Integer totalReminders;
    private String treatmentDuration; // e.g., "30 days"
}
