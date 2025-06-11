package com.fpt.hivtreatment.service;

import com.fpt.hivtreatment.model.entity.LabTestOrder;
import com.fpt.hivtreatment.model.entity.TestType;
import com.fpt.hivtreatment.model.entity.User;
import com.fpt.hivtreatment.repository.LabTestOrderRepository;
import com.fpt.hivtreatment.repository.TestTypeRepository;
import com.fpt.hivtreatment.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface LabTestOrderService {

    /**
     * Tạo đơn xét nghiệm mới
     */
    LabTestOrder createLabTestOrder(Long testTypeId, Long patientId, Long doctorId, Long medicalRecordId, String notes);

    /**
     * Lấy danh sách đơn xét nghiệm của bệnh nhân
     */
    List<LabTestOrder> getPatientOrders(Long patientId);

    /**
     * Lấy danh sách đơn xét nghiệm HIV của bệnh nhân
     */
    List<LabTestOrder> getPatientHIVOrders(Long patientId);

    /**
     * Lấy danh sách đơn xét nghiệm theo hồ sơ y tế
     */
    List<LabTestOrder> getOrdersByMedicalRecord(Long medicalRecordId);

    /**
     * Cập nhật trạng thái đơn xét nghiệm
     */
    LabTestOrder updateOrderStatus(Long orderId, String newStatus);

    /**
     * Lấy đơn xét nghiệm theo ID
     */
    Optional<LabTestOrder> getOrderById(Long orderId);

    /**
     * Lấy danh sách đơn hàng theo trạng thái
     */
    List<LabTestOrder> getOrdersByStatus(String status);

    /**
     * Đếm số đơn hàng theo trạng thái
     */
    Long countOrdersByStatus(String status);

    /**
     * Cập nhật ngày dự kiến trả kết quả xét nghiệm
     */
    LabTestOrder updateExpectedResultDate(Long orderId, String resultExpectedDate, String notes);
}
