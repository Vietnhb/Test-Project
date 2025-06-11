package com.fpt.hivtreatment.payload.request;

import lombok.Data;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;

@Data
public class PrescriptionItemRequest {
    
    @NotNull(message = "Medication ID is required")
    private Long medicationId;
    
    @Min(value = 0, message = "Morning dose cannot be negative")
    private Double morningDose = 0.0;
    
    @Min(value = 0, message = "Noon dose cannot be negative")
    private Double noonDose = 0.0;
    
    @Min(value = 0, message = "Afternoon dose cannot be negative")
    private Double afternoonDose = 0.0;
    
    @Min(value = 0, message = "Evening dose cannot be negative")
    private Double eveningDose = 0.0;
    
    private String instructions;
    
    private String status = "active";
}
