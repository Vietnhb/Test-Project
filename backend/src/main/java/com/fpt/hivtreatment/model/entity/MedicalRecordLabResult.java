package com.fpt.hivtreatment.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "medical_record_lab_results", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "medical_record_id", "lab_test_result_id" }, name = "unique_record_result")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedicalRecordLabResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medical_record_id", nullable = false)
    private MedicalRecord medicalRecord;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lab_test_result_id", nullable = false)
    private LabTestResult labTestResult;

    @Column(name = "result_interpretation", columnDefinition = "TEXT")
    private String resultInterpretation;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}