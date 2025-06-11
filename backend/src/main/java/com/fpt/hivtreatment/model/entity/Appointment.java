package com.fpt.hivtreatment.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.Date;

@Entity
@Table(name = "appointment")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Appointment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "patient_id")
    private User patient;

    @ManyToOne
    @JoinColumn(name = "doctor_id", nullable = false)
    private DoctorProfile doctor;

    @ManyToOne
    @JoinColumn(name = "appointment_slot_id", nullable = false)
    private AppointmentSlot appointmentSlot;

    @Column(name = "appointment_type", nullable = false)
    private String appointmentType;

<<<<<<< HEAD
    @Column(name = "status", length = 20)
    @Builder.Default
    private String status = "Đã đặt"; // 'Đã đặt', 'Đã xác nhận', 'Đã hủy', 'Hoàn thành'

    @Column(name = "is_anonymous")
    @Builder.Default
    private Boolean isAnonymous = false;

    @Column(name = "is_virtual")
    @Builder.Default
    private Boolean isVirtual = false;

=======
    @Column(name = "status")
    private String status = "Chờ xác nhận";

    @Column(name = "is_anonymous")
    private Boolean isAnonymous = false;

>>>>>>> fd42c148e0431975301ca683137e9cc7dea64a1c
    @Column(name = "symptoms")
    private String symptoms;

    @Column(name = "notes")
    private String notes;

    @Column(name = "cancellation_reason")
    private String cancellationReason;

    @CreatedDate
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at", updatable = false)
    private Date createdAt;
}