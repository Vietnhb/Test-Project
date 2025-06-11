package com.fpt.hivtreatment.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "lab_test_results")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LabTestResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "lab_test_order_id", nullable = false, unique = true)
    private LabTestOrder labTestOrder;

    @Column(name = "result_date")
    private LocalDateTime resultDate;

    @Column(name = "result_data", columnDefinition = "TEXT", nullable = false)
    private String resultData;

    @Column(name = "result_summary", length = 500)
    private String resultSummary;

    @Column(name = "attachments_path", columnDefinition = "TEXT")
    private String attachmentsPath;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @ManyToOne
    @JoinColumn(name = "entered_by_user_id")
    private User enteredByUser;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}