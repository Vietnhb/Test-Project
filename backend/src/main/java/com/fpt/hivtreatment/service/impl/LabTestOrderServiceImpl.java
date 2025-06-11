package com.fpt.hivtreatment.service.impl;

import com.fpt.hivtreatment.model.entity.LabTestOrder;
import com.fpt.hivtreatment.model.entity.TestType;
import com.fpt.hivtreatment.model.entity.User;
import com.fpt.hivtreatment.repository.LabTestOrderRepository;
import com.fpt.hivtreatment.repository.TestTypeRepository;
import com.fpt.hivtreatment.repository.UserRepository;
import com.fpt.hivtreatment.service.LabTestOrderService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class LabTestOrderServiceImpl implements LabTestOrderService {

    @Autowired
    private LabTestOrderRepository labTestOrderRepository;

    @Autowired
    private TestTypeRepository testTypeRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Tạo đơn xét nghiệm mới
     */
    @Override
    public LabTestOrder createLabTestOrder(Long testTypeId, Long patientId, Long doctorId, Long medicalRecordId,
            String notes) {
        // Log để debug các tham số nhận được
        System.out.println("Creating lab test order with params: testTypeId=" + testTypeId
                + ", patientId=" + patientId
                + ", doctorId=" + doctorId
                + ", medicalRecordId=" + medicalRecordId);

        // Kiểm tra loại xét nghiệm
        Optional<TestType> testTypeOpt = testTypeRepository.findById(testTypeId);
        if (testTypeOpt.isEmpty()) {
            throw new RuntimeException("Không tìm thấy loại xét nghiệm");
        }

        // Kiểm tra bệnh nhân
        Optional<User> patientOpt = userRepository.findById(patientId);
        if (patientOpt.isEmpty()) {
            throw new RuntimeException("Không tìm thấy bệnh nhân");
        }

        // Kiểm tra bác sĩ (nếu có)
        User doctor = null;
        if (doctorId != null) {
            Optional<User> doctorOpt = userRepository.findById(doctorId);
            if (doctorOpt.isPresent()) {
                doctor = doctorOpt.get();
            } else {
                System.out.println("Warning: Doctor with ID " + doctorId + " not found");
            }
        }

        TestType testType = testTypeOpt.get();
        User patient = patientOpt.get();

        // Tạo đơn hàng mới
        LabTestOrder order = new LabTestOrder();
        order.setTestType(testType);
        order.setPatient(patient);
        order.setDoctor(doctor);
        order.setOrderDate(LocalDate.now());
        order.setNotes(notes);
        order.setStatus("Chờ thanh toán");

        // Thêm medical record ID nếu có
        if (medicalRecordId != null) {
            order.setMedicalRecordId(medicalRecordId);
            System.out.println("Set medical record ID: " + medicalRecordId);
        }

        // Không cần thiết lập result_expected_date theo yêu cầu
        // Không cần thiết lập priority vì đã có giá trị mặc định trong entity
        // Không cần thiết lập created_at vì được xử lý bởi @PrePersist trong entity

        return labTestOrderRepository.save(order);
    }

    /**
     * Lấy danh sách đơn xét nghiệm của bệnh nhân
     */
    @Override
    public List<LabTestOrder> getPatientOrders(Long patientId) {
        Optional<User> patientOpt = userRepository.findById(patientId);
        if (patientOpt.isEmpty()) {
            throw new RuntimeException("Không tìm thấy bệnh nhân");
        }
        return labTestOrderRepository.findByPatientOrderByOrderDateDesc(patientOpt.get());
    }

    /**
     * Lấy danh sách đơn xét nghiệm HIV của bệnh nhân
     */
    @Override
    public List<LabTestOrder> getPatientHIVOrders(Long patientId) {
        Optional<User> patientOpt = userRepository.findById(patientId);
        if (patientOpt.isEmpty()) {
            throw new RuntimeException("Không tìm thấy bệnh nhân");
        }
        return labTestOrderRepository.findHIVTestOrdersByPatient(patientOpt.get());
    }

    /**
     * Lấy danh sách đơn xét nghiệm theo hồ sơ y tế
     */
    @Override
    public List<LabTestOrder> getOrdersByMedicalRecord(Long medicalRecordId) {
        if (medicalRecordId == null) {
            throw new RuntimeException("medical_record_id không được để trống");
        }

        System.out.println("Getting lab test orders for medical record ID: " + medicalRecordId);

        List<LabTestOrder> orders = labTestOrderRepository.findByMedicalRecordId(medicalRecordId);

        System.out.println("Found " + orders.size() + " lab test orders for medical record ID: " + medicalRecordId);
        if (orders.isEmpty()) {
            System.out.println("No orders found for medical record ID: " + medicalRecordId);
        } else {
            for (LabTestOrder order : orders) {
                System.out.println("Order ID: " + order.getId() + ", Medical Record ID: " + order.getMedicalRecordId()
                        + ", Status: " + order.getStatus());
            }
        }

        return orders;
    }

    /**
     * Cập nhật trạng thái đơn xét nghiệm
     */
    @Override
    public LabTestOrder updateOrderStatus(Long orderId, String newStatus) {
        Optional<LabTestOrder> orderOpt = labTestOrderRepository.findById(orderId);
        if (orderOpt.isEmpty()) {
            throw new RuntimeException("Không tìm thấy đơn xét nghiệm");
        }

        LabTestOrder order = orderOpt.get();
        order.setStatus(newStatus);

        return labTestOrderRepository.save(order);
    }

    /**
     * Lấy đơn xét nghiệm theo ID
     */
    @Override
    public Optional<LabTestOrder> getOrderById(Long orderId) {
        return labTestOrderRepository.findById(orderId);
    }

    /**
     * Lấy danh sách đơn hàng theo trạng thái
     */
    @Override
    public List<LabTestOrder> getOrdersByStatus(String status) {
        return labTestOrderRepository.findByStatusOrderByOrderDateDesc(status);
    }

    /**
     * Đếm số đơn hàng theo trạng thái
     */
    @Override
    public Long countOrdersByStatus(String status) {
        return labTestOrderRepository.countByStatus(status);
    }

    /**
     * Cập nhật ngày dự kiến trả kết quả xét nghiệm
     */
    @Override
    public LabTestOrder updateExpectedResultDate(Long orderId, String resultExpectedDate, String notes) {
        Optional<LabTestOrder> orderOpt = labTestOrderRepository.findById(orderId);
        if (orderOpt.isEmpty()) {
            throw new RuntimeException("Không tìm thấy đơn xét nghiệm");
        }

        LabTestOrder order = orderOpt.get();

        // Parse the date string to LocalDateTime
        try {
            LocalDateTime expectedDate = LocalDate.parse(resultExpectedDate).atTime(17, 0); // Default to 5:00 PM
            order.setResultExpectedDate(expectedDate);

            // Update notes if provided
            if (notes != null && !notes.trim().isEmpty()) {
                order.setNotes(notes);
            }

            return labTestOrderRepository.save(order);
        } catch (Exception e) {
            throw new RuntimeException("Định dạng ngày không hợp lệ: " + e.getMessage());
        }
    }
}