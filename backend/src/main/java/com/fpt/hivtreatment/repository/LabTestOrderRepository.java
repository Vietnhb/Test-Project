package com.fpt.hivtreatment.repository;

import com.fpt.hivtreatment.model.entity.LabTestOrder;
import com.fpt.hivtreatment.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LabTestOrderRepository extends JpaRepository<LabTestOrder, Long> {

        /**
         * Tìm kiếm đơn hàng theo bệnh nhân
         */
        List<LabTestOrder> findByPatientOrderByOrderDateDesc(User patient);

        /**
         * Tìm kiếm đơn hàng theo bệnh nhân và trạng thái
         */
        List<LabTestOrder> findByPatientAndStatusOrderByOrderDateDesc(User patient, String status);

        /**
         * Tìm kiếm đơn hàng theo bác sĩ
         */
        List<LabTestOrder> findByDoctorOrderByOrderDateDesc(User doctor);

        /**
         * Tìm kiếm đơn hàng theo trạng thái
         */
        List<LabTestOrder> findByStatusOrderByOrderDateDesc(String status);

        /**
         * Tìm kiếm đơn hàng theo ngày tạo
         */
        List<LabTestOrder> findByOrderDateBetweenOrderByOrderDateDesc(LocalDate startDate, LocalDate endDate);

        /**
         * Tìm kiếm đơn hàng xét nghiệm HIV của bệnh nhân
         */
        @Query("SELECT lto FROM LabTestOrder lto " +
                        "JOIN lto.testType tt " +
                        "WHERE lto.patient = :patient AND tt.testGroup = 'HIV' " +
                        "ORDER BY lto.orderDate DESC")
        List<LabTestOrder> findHIVTestOrdersByPatient(@Param("patient") User patient);

        /**
         * Đếm số đơn hàng theo trạng thái
         */
        @Query("SELECT COUNT(lto) FROM LabTestOrder lto WHERE lto.status = :status")
        Long countByStatus(@Param("status") String status);

        List<LabTestOrder> findByPatientId(Long patientId);

        List<LabTestOrder> findByDoctorId(Long doctorId);

        List<LabTestOrder> findByStatus(String status);

        List<LabTestOrder> findByMedicalRecordId(Long medicalRecordId);
}
