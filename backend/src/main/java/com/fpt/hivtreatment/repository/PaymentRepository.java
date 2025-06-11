package com.fpt.hivtreatment.repository;

import com.fpt.hivtreatment.model.entity.Payment;
import com.fpt.hivtreatment.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    /**
     * Find payments by patient
     */
    List<Payment> findByPatientOrderByCreatedAtDesc(User patient);

    /**
     * Find payments by patient ID
     */
    @Query("SELECT p FROM Payment p WHERE p.patient.id = :patientId ORDER BY p.createdAt DESC")
    List<Payment> findByPatientIdOrderByCreatedAtDesc(@Param("patientId") Long patientId);

    /**
     * Find payments by status
     */
    List<Payment> findByStatusOrderByCreatedAtDesc(String status);

    /**
     * Count payments by status
     */
    long countByStatus(String status);
}