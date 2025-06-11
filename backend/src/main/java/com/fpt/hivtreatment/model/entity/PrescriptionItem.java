package com.fpt.hivtreatment.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Formula;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "prescription_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrescriptionItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "prescription_id", nullable = false)
    private Long prescriptionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prescription_id", insertable = false, updatable = false)
    private Prescription prescription;

    @Column(name = "medication_id", nullable = false)
    private Long medicationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medication_id", insertable = false, updatable = false)
    private Medication medication;

    // Liều lượng theo từng buổi trong ngày (từ giao diện)
    @Column(name = "morning_dose")
    private Integer morningDose = 0;

    @Column(name = "noon_dose")
    private Integer noonDose = 0;

    @Column(name = "afternoon_dose")
    private Integer afternoonDose = 0;

    @Column(name = "evening_dose")
    private Integer eveningDose = 0;

    // Tính toán tự động (computed field)
    @Formula("morning_dose + noon_dose + afternoon_dose + evening_dose")
    private Integer dailyTotal;

    // Số lượng thuốc và đơn vị
    @Column(name = "total_quantity")
    private Integer totalQuantity;

    @Column(name = "unit", length = 20)
    private String unit = "viên";

    // Hướng dẫn sử dụng
    @Column(name = "usage_instructions", columnDefinition = "TEXT")
    private String usageInstructions;

    @Column(name = "special_notes", columnDefinition = "TEXT")
    private String specialNotes;

    // Thời gian
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Relationships
    @OneToMany(mappedBy = "prescriptionItem", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<MedicationReminder> medicationReminders;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Helper method để tính dailyTotal (nếu không dùng @Formula)
    public Integer calculateDailyTotal() {
        return (morningDose != null ? morningDose : 0) +
                (noonDose != null ? noonDose : 0) +
                (afternoonDose != null ? afternoonDose : 0) +
                (eveningDose != null ? eveningDose : 0);
    }
}
