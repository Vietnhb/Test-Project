package com.fpt.hivtreatment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LabTestResultDTO {
    private Long id;
    private Long labTestOrderId;
    private LocalDateTime resultDate;
    private LocalDateTime performedDate; // Ngày thực hiện xét nghiệm (từ order date)
    private String resultData; // JSON format data
    private String resultSummary;
    private String attachmentsPath;
    private String notes;
    private Long enteredByUserId;
    private String enteredByUserName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt; // Additional fields for convenient frontend display
    private String patientName;
    private Long patientId;
    private String testTypeName;
    private Long testTypeId;
    private String status;
    private Long medicalRecordId;
    private Long testOrderId; // Alias for labTestOrderId for frontend compatibility

    // Test details
    private String testCategory;
    private String testGroup;
    private String referenceRange;
    private String testValue;
    private String testUnit;

    // Parsed attachments - for easier frontend use
    private String parsedAttachments; // JSON array of attachment objects
}