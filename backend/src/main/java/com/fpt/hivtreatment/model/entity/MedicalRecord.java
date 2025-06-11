package com.fpt.hivtreatment.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "medical_records")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class MedicalRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "patient_id", nullable = false)
    private User patient;

    @ManyToOne
    @JoinColumn(name = "doctor_id")
    private User doctor;

    @Column(name = "visit_date", nullable = false)
    private LocalDate visitDate;    @Column(name = "visit_type", length = 50)
    @Builder.Default
    private String visitType = "Khám bệnh";

    @Column(name = "record_status", length = 50)
    @Builder.Default
    private String recordStatus = "Đang điều trị";

    // Clinical examination details
    @Column(name = "symptoms", columnDefinition = "TEXT")
    private String symptoms;

    @Column(name = "lymph_nodes", columnDefinition = "TEXT")
    private String lymphNodes;

    @Column(name = "blood_pressure", length = 20)
    private String bloodPressure;

    @Column(name = "weight")
    private Double weight;

    @Column(name = "general_condition", columnDefinition = "TEXT")
    private String generalCondition;

    // Assessment and plan
    @Column(name = "diagnosis", columnDefinition = "TEXT")
    private String diagnosis;

    // Treatment protocol
    @ManyToOne
    @JoinColumn(name = "primary_protocol_id")
    private TreatmentProtocol primaryProtocol;

    @Column(name = "protocol_start_date")
    private LocalDate protocolStartDate;

    // HIV specific information
    @Column(name = "who_clinical_stage", length = 50)
    private String whoClinicalStage;

    @Column(name = "opportunistic_infections", columnDefinition = "TEXT")
    private String opportunisticInfections;

    @Column(name = "protocol_notes", columnDefinition = "TEXT")
    private String protocolNotes;    @Column(name = "risk_factors", columnDefinition = "TEXT")
    private String riskFactors;

    @Column(name = "next_appointment_date")
    private LocalDate nextAppointmentDate;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Relationships
    @OneToMany(mappedBy = "medicalRecord", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Prescription> prescriptions;

    @OneToMany(mappedBy = "medicalRecord", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Reminder> reminders;
}