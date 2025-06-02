package com.fpt.hivtreatment.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "doctor_profile")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class DoctorProfile {
    @Id
    @Column(name = "doctor_id")
    private Long doctorId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "doctor_id")
    private User user;

    @Column(name = "specialty", nullable = false)
    private String specialty;

    @Column(name = "qualification")
    private String qualification;

    @Column(name = "experience")
    private Integer experience;

    @Column(name = "bio")
    private String bio;
}