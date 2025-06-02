package com.fpt.hivtreatment.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "roles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role {
    // Constants for role IDs
    public static final int ROLE_PATIENT = 1;
    public static final int ROLE_DOCTOR = 2;
    public static final int ROLE_STAFF = 3;
    public static final int ROLE_ADMIN = 4;
    public static final int ROLE_MANAGER = 5;

    // Constants for role names
    public static final String PATIENT = "PATIENT";
    public static final String DOCTOR = "DOCTOR";
    public static final String STAFF = "STAFF";
    public static final String ADMIN = "ADMIN";
    public static final String MANAGER = "MANAGER";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 20, unique = true, nullable = false)
    private String name;
}