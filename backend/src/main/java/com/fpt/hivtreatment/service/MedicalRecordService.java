package com.fpt.hivtreatment.service;

import com.fpt.hivtreatment.dto.MedicalRecordDTO;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface MedicalRecordService {

    /**
     * Create a new medical record
     * 
     * @param medicalRecordDTO the medical record data
     * @return the created medical record
     */
    MedicalRecordDTO createMedicalRecord(MedicalRecordDTO medicalRecordDTO);

    /**
     * Update an existing medical record
     * 
     * @param id               the record ID
     * @param medicalRecordDTO the updated medical record data
     * @return the updated medical record
     */
    MedicalRecordDTO updateMedicalRecord(Long id, MedicalRecordDTO medicalRecordDTO);

    /**
     * Get a medical record by ID
     * 
     * @param id the record ID
     * @return the medical record
     */
    MedicalRecordDTO getMedicalRecordById(Long id);

    /**
     * Get all medical records for a patient
     * 
     * @param patientId the patient ID
     * @param page      the page number
     * @param size      the page size
     * @return list of medical records with pagination info
     */
    Map<String, Object> getMedicalRecordsByPatient(Long patientId, int page, int size);

    /**
     * Get all medical records for a doctor
     * 
     * @param doctorId the doctor ID
     * @param page     the page number
     * @param size     the page size
     * @return list of medical records with pagination info
     */
    Map<String, Object> getMedicalRecordsByDoctor(Long doctorId, int page, int size);

    /**
     * Get medical records for a patient within a date range
     * 
     * @param patientId the patient ID
     * @param startDate the start date
     * @param endDate   the end date
     * @return list of medical records
     */
    List<MedicalRecordDTO> getMedicalRecordsByPatientAndDateRange(Long patientId, LocalDate startDate,
            LocalDate endDate);

    /**
     * Get medical records for a doctor on a specific date
     * 
     * @param doctorId the doctor ID
     * @param date     the date
     * @return list of medical records
     */
    List<MedicalRecordDTO> getMedicalRecordsByDoctorAndDate(Long doctorId, LocalDate date);

    /**
     * Delete a medical record
     * 
     * @param id the record ID
     */
    void deleteMedicalRecord(Long id);

    /**
     * Update the status of a medical record
     * 
     * @param id     the record ID
     * @param status the new status
     * @return the updated medical record
     */
    MedicalRecordDTO updateMedicalRecordStatus(Long id, String status);

    /**
     * Count medical records by status
     * 
     * @param status the record status
     * @return the count
     */
    long countMedicalRecordsByStatus(String status);
}