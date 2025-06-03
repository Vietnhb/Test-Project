package com.fpt.hivtreatment.service;

import com.fpt.hivtreatment.payload.request.AppointmentRequest;
import com.fpt.hivtreatment.payload.response.AppointmentResponse;

import java.util.List;
import java.util.Map;

public interface AppointmentService {
    /**
     * Create a new appointment and mark the slot as unavailable
     * 
     * @param request the appointment details
     * @return the created appointment
     */
    AppointmentResponse createAppointment(AppointmentRequest request);

    /**
     * Update appointment status
     * 
     * @param id     the appointment id
     * @param status the new status
     * @return the updated appointment
     */
    AppointmentResponse updateStatus(Long id, String status);

    /**
     * Get appointments by patient id
     * 
     * @param patientId the patient id
     * @return list of appointments
     */
    List<AppointmentResponse> getAppointmentsByPatient(Long patientId);

    /**
     * Get appointments by doctor id
     * 
     * @param doctorId the doctor id
     * @return list of appointments
     */
    List<AppointmentResponse> getAppointmentsByDoctor(Long doctorId);

    /**
     * Get appointments by patient id and status
     * 
     * @param patientId the patient id
     * @param status    the appointment status
     * @return list of appointments
     */
    List<AppointmentResponse> getAppointmentsByPatientAndStatus(Long patientId, String status);

    /**
     * Get appointments by doctor id and status
     * 
     * @param doctorId the doctor id
     * @param status   the appointment status
     * @return list of appointments
     */
    List<AppointmentResponse> getAppointmentsByDoctorAndStatus(Long doctorId, String status);

    /**
     * Get appointment by id
     * 
     * @param id the appointment id
     * @return the appointment
     */
    AppointmentResponse getAppointmentById(Long id);

    /**
     * Count appointments by status
     * 
     * @param status the appointment status
     * @return the number of appointments with the given status
     */
    long countAppointmentsByStatus(String status);

    /**
     * Get appointments for manager dashboard
     * 
     * @param status    optional filter by status
     * @param startDate optional filter by start date
     * @param endDate   optional filter by end date
     * @param page      page number for pagination
     * @param size      page size for pagination
     * @param limit     limit the number of results
     * @return map containing appointments and pagination info
     */
    Map<String, Object> getAppointmentsForManager(String status, String startDate, String endDate, int page, int size,
            int limit);

    /**
     * Update appointment status with optional cancellation reason
     * 
     * @param id                 appointment id
     * @param status             new status
     * @param cancellationReason optional reason for cancellation
     * @return map containing updated appointment
     */
    Map<String, Object> updateAppointmentStatus(Long id, String status, String cancellationReason);
}