package com.fpt.hivtreatment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Data Transfer Object for Medical Records
 * Maps to the medical_records table in the database
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicalRecordDTO {

    private Long id; // id
    private Long patientId; // patient_id
    private String patientName; // Not in table, for display
    private Long doctorId; // doctor_id
    private String doctorName; // Not in table, for display
    private LocalDate visitDate; // visit_date
    private String visitType; // visit_type
    private String recordStatus; // record_status

    // Clinical examination details
    private String symptoms; // symptoms
    private String lymphNodes; // lymph_nodes
    private String bloodPressure; // blood_pressure
    private String generalCondition; // general_condition
    private BigDecimal weight; // weight
    private String riskFactors; // risk_factors

    // Assessment and plan
    private String diagnosis; // diagnosis

    // Treatment protocol information
    private Long primaryProtocolId; // primary_protocol_id
    private String protocolName; // Not in table, for display
    private LocalDate protocolStartDate; // protocol_start_date

    // HIV specific information
    private String whoClinicalStage; // who_clinical_stage
    private String opportunisticInfections; // opportunistic_infections
    private String protocolNotes; // protocol_notes

    // Additional fields
    private LocalDate nextAppointmentDate; // next_appointment_date
    private String notes; // notes
    private LocalDateTime createdAt; // created_at
    private LocalDateTime updatedAt; // updated_at
}