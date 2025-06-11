package com.fpt.hivtreatment.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "test_types")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "category", length = 50)
    private String category; // 'Sàng lọc', 'Khẳng định', 'Theo dõi', 'Hỗ trợ'

    @Column(name = "test_group", length = 100)
    private String testGroup; // 'HIV', 'Hematology', 'Biochemistry', 'Immunology'

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "default_turnaround_time_hours")
    private Integer defaultTurnaroundTimeHours;

    @Column(name = "requires_fasting")
    @Builder.Default
    private Boolean requiresFasting = false;

    @Column(name = "specimen_type", length = 50)
    @Builder.Default
    private String specimenType = "Máu";

    @Column(name = "active")
    @Builder.Default
    private Boolean active = true;

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
