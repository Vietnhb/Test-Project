package com.fpt.hivtreatment.payload.request;

import lombok.Data;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Future;
import java.time.LocalDate;
import java.util.List;

@Data
public class PrescriptionRequest {
    
    @NotNull(message = "Patient ID is required")
    private Long patientId;
    
    @NotNull(message = "Medical record ID is required")
    private Long medicalRecordId;
    
    @NotNull(message = "Doctor ID is required")
    private Long doctorId;
    
    private Long protocolId;
    
    @NotNull(message = "Start date is required")
    private LocalDate startDate;
    
    @NotNull(message = "End date is required")
    @Future(message = "End date must be in the future")
    private LocalDate endDate;
    
    private String doctorNotes;
    
    private String status = "active";
    
    @NotNull(message = "Prescription items are required")
    private List<PrescriptionItemRequest> prescriptionItems;
}
