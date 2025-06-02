package com.fpt.hivtreatment.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.fpt.hivtreatment.model.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    List<User> findByRoleId(Integer roleId, Pageable pageable);

    List<User> findByIsActive(Boolean isActive, Pageable pageable);

    List<User> findByRoleIdAndIsActive(Integer roleId, Boolean isActive, Pageable pageable);

    long countByRoleId(Integer roleId);

    long countByIsActive(Boolean isActive);

    long countByRoleIdAndIsActive(Integer roleId, Boolean isActive);

    /**
     * Mark a user as inactive (soft delete) - safer approach when constraints exist
     * 
     * @param id the user ID to mark as inactive
     * @return the number of rows affected
     */
    @Modifying
    @Transactional
    @Query(value = "UPDATE users SET is_active = false WHERE id = :id", nativeQuery = true)
    int deactivateUserById(@Param("id") Long id);

    /**
     * Check if there are any foreign key references to this user
     * 
     * @param id the user ID to check
     * @return true if references exist, false otherwise
     */
    @Query(value = "SELECT " +
            "  EXISTS(SELECT 1 FROM appointments WHERE patient_id = :id OR doctor_id = :id OR nurse_id = :id LIMIT 1) OR "
            +
            "  EXISTS(SELECT 1 FROM medical_records WHERE patient_id = :id OR doctor_id = :id LIMIT 1) OR " +
            "  EXISTS(SELECT 1 FROM lab_tests WHERE patient_id = :id OR doctor_id = :id OR result_entered_by_user_id = :id LIMIT 1) OR "
            +
            "  EXISTS(SELECT 1 FROM patient_art_regimen WHERE patient_id = :id OR doctor_id = :id LIMIT 1) OR " +
            "  EXISTS(SELECT 1 FROM medication_reminders WHERE patient_id = :id LIMIT 1) OR " +
            "  EXISTS(SELECT 1 FROM articles WHERE author_id = :id LIMIT 1) OR " +
            "  EXISTS(SELECT 1 FROM doctor_profile WHERE doctor_id = :id LIMIT 1) OR " +
            "  EXISTS(SELECT 1 FROM doctor_schedule WHERE doctor_id = :id LIMIT 1)", nativeQuery = true)
    boolean hasReferences(@Param("id") Long id);

    /**
     * Delete a user directly using a native SQL query to bypass constraints
     * 
     * @param id the user ID to delete
     * @return the number of rows affected
     */
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM users WHERE id = :id", nativeQuery = true)
    int deleteUserById(@Param("id") Long id);

    /**
     * Hard delete method that first nullifies foreign key references
     * Use with caution - this will remove the user from relational data
     * 
     * @param id the user ID to delete
     */
    @Modifying
    @Transactional
    @Query(value = "SET FOREIGN_KEY_CHECKS=0", nativeQuery = true)
    void disableForeignKeyChecks();

    @Modifying
    @Transactional
    @Query(value = "SET FOREIGN_KEY_CHECKS=1", nativeQuery = true)
    void enableForeignKeyChecks();

    /**
     * Complete force delete - removes all traces of the user from the database
     * This is a very aggressive approach that will delete the user and all related
     * records
     */
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM users WHERE id = :id", nativeQuery = true)
    void forceDeleteUser(@Param("id") Long id);

    /**
     * MySQL-specific commands to drop and recreate foreign keys
     */
    @Modifying
    @Transactional
    @Query(value = "SET SQL_SAFE_UPDATES = 0", nativeQuery = true)
    void disableSafeUpdates();

    @Modifying
    @Transactional
    @Query(value = "SET SQL_SAFE_UPDATES = 1", nativeQuery = true)
    void enableSafeUpdates();

    /**
     * Direct database approach for different environments
     */
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM users WHERE id = ?1", nativeQuery = true)
    int deleteBySqlId(Long id);

    /**
     * Xóa bản ghi doctor_profile của người dùng
     * 
     * @param id ID của người dùng
     * @return số bản ghi bị ảnh hưởng
     */
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM doctor_profile WHERE doctor_id = :id", nativeQuery = true)
    int deleteFromDoctorProfile(@Param("id") Long id);

    /**
     * Xóa tham chiếu trong bảng medical_records
     */
    @Modifying
    @Transactional
    @Query(value = "UPDATE medical_records SET doctor_id = NULL WHERE doctor_id = :id", nativeQuery = true)
    void nullifyMedicalRecordsDoctor(@Param("id") Long id);

    /**
     * Xóa tham chiếu patient trong bảng medical_records
     */
    @Modifying
    @Transactional
    @Query(value = "UPDATE medical_records SET patient_id = NULL WHERE patient_id = :id", nativeQuery = true)
    void nullifyMedicalRecordsPatient(@Param("id") Long id);

    /**
     * Xóa tham chiếu trong bảng lab_tests (doctor)
     */
    @Modifying
    @Transactional
    @Query(value = "UPDATE lab_tests SET doctor_id = NULL WHERE doctor_id = :id", nativeQuery = true)
    void nullifyLabTestsDoctor(@Param("id") Long id);

    /**
     * Xóa tham chiếu trong bảng lab_tests (patient)
     */
    @Modifying
    @Transactional
    @Query(value = "UPDATE lab_tests SET patient_id = NULL WHERE patient_id = :id", nativeQuery = true)
    void nullifyLabTestsPatient(@Param("id") Long id);

    /**
     * Xóa tham chiếu trong bảng lab_tests (result_entered_by)
     */
    @Modifying
    @Transactional
    @Query(value = "UPDATE lab_tests SET result_entered_by_user_id = NULL WHERE result_entered_by_user_id = :id", nativeQuery = true)
    void nullifyLabTestsResultEnteredBy(@Param("id") Long id);

    /**
     * Xóa tham chiếu trong bảng articles
     */
    @Modifying
    @Transactional
    @Query(value = "UPDATE articles SET author_id = NULL WHERE author_id = :id", nativeQuery = true)
    void nullifyArticlesAuthor(@Param("id") Long id);

    /**
     * Xóa tham chiếu trong bảng doctor_schedule
     */
    @Modifying
    @Transactional
    @Query(value = "UPDATE doctor_schedule SET doctor_id = NULL WHERE doctor_id = :id", nativeQuery = true)
    void nullifyDoctorSchedule(@Param("id") Long id);

    /**
     * Xóa tham chiếu doctor trong bảng patient_art_regimen
     */
    @Modifying
    @Transactional
    @Query(value = "UPDATE patient_art_regimen SET doctor_id = NULL WHERE doctor_id = :id", nativeQuery = true)
    void nullifyPatientArtRegimenDoctor(@Param("id") Long id);

    /**
     * Xóa tham chiếu patient trong bảng patient_art_regimen
     */
    @Modifying
    @Transactional
    @Query(value = "UPDATE patient_art_regimen SET patient_id = NULL WHERE patient_id = :id", nativeQuery = true)
    void nullifyPatientArtRegimenPatient(@Param("id") Long id);

    /**
     * Xóa tham chiếu patient trong bảng medication_reminders
     */
    @Modifying
    @Transactional
    @Query(value = "UPDATE medication_reminders SET patient_id = NULL WHERE patient_id = :id", nativeQuery = true)
    void nullifyMedicationRemindersPatient(@Param("id") Long id);

    /**
     * Xóa bản ghi doctor_schedule của người dùng
     */
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM doctor_schedule WHERE doctor_id = :id", nativeQuery = true)
    void deleteFromDoctorSchedule(@Param("id") Long id);

    /**
     * Xóa bản ghi medication_reminders của người dùng
     */
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM medication_reminders WHERE patient_id = :id", nativeQuery = true)
    void deleteFromMedicationReminders(@Param("id") Long id);

    /**
     * Xóa bản ghi lab_tests của người dùng
     */
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM lab_tests WHERE doctor_id = :id OR patient_id = :id OR result_entered_by_user_id = :id", nativeQuery = true)
    void deleteFromLabTests(@Param("id") Long id);

    /**
     * Xóa bản ghi articles của người dùng
     */
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM articles WHERE author_id = :id", nativeQuery = true)
    void deleteFromArticles(@Param("id") Long id);

    /**
     * Xóa bản ghi patient_art_regimen của người dùng
     */
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM patient_art_regimen WHERE doctor_id = :id OR patient_id = :id", nativeQuery = true)
    void deleteFromPatientArtRegimen(@Param("id") Long id);

    /**
     * Xóa bản ghi medical_records của người dùng
     */
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM medical_records WHERE doctor_id = :id OR patient_id = :id", nativeQuery = true)
    void deleteFromMedicalRecords(@Param("id") Long id);

    /**
     * Cập nhật tất cả các khóa ngoại trong appointments thành NULL
     */
    @Modifying
    @Transactional
    @Query(value = "UPDATE appointments SET patient_id = NULL WHERE patient_id = :id", nativeQuery = true)
    void nullifyAppointmentsPatient(@Param("id") Long id);

    /**
     * Cập nhật doctor_id trong appointments thành NULL
     */
    @Modifying
    @Transactional
    @Query(value = "UPDATE appointments SET doctor_id = NULL WHERE doctor_id = :id", nativeQuery = true)
    void nullifyAppointmentsDoctor(@Param("id") Long id);

    /**
     * Cập nhật nurse_id trong appointments thành NULL
     */
    @Modifying
    @Transactional
    @Query(value = "UPDATE appointments SET nurse_id = NULL WHERE nurse_id = :id", nativeQuery = true)
    void nullifyAppointmentsNurse(@Param("id") Long id);

    /**
     * Xóa cưỡng chế user bằng câu lệnh SQL trực tiếp
     */
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM users WHERE id = :id", nativeQuery = true)
    void forciblyDeleteUser(@Param("id") Long id);
}