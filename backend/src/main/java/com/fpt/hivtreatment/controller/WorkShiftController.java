package com.fpt.hivtreatment.controller;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.fpt.hivtreatment.model.entity.WorkShift;
import com.fpt.hivtreatment.repository.WorkShiftRepository;

import lombok.RequiredArgsConstructor;

/**
 * Controller cho quản lý ca làm việc (WorkShift)
 * API dùng chung cho manager và các vai trò khác
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class WorkShiftController {

    private static final Logger logger = LoggerFactory.getLogger(WorkShiftController.class);

    private final WorkShiftRepository workShiftRepository;

    /**
     * API lấy danh sách tất cả ca làm việc
     * Endpoint này không yêu cầu phân quyền, có thể truy cập bởi tất cả người dùng
     * đã đăng nhập
     */
    @GetMapping("/work-shifts")
    public ResponseEntity<?> getAllWorkShifts() {
        logger.info("Nhận yêu cầu lấy danh sách tất cả ca làm việc");

        try {
            // Lấy danh sách tất cả ca làm việc đang active
            List<WorkShift> workShifts = workShiftRepository.findByIsActive(true);

            return ResponseEntity.ok(workShifts);
        } catch (Exception e) {
            logger.error("Lỗi khi lấy danh sách ca làm việc", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Lỗi khi lấy danh sách ca làm việc: " + e.getMessage()));
        }
    }

    /**
     * API lấy thông tin chi tiết một ca làm việc theo ID
     */
    @GetMapping("/work-shifts/{id}")
    public ResponseEntity<?> getWorkShiftById(@PathVariable Long id) {
        logger.info("Nhận yêu cầu lấy thông tin ca làm việc với id: {}", id);

        try {
            WorkShift workShift = workShiftRepository.findById(id)
                    .orElse(null);

            if (workShift == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "Không tìm thấy ca làm việc với id: " + id));
            }

            return ResponseEntity.ok(workShift);
        } catch (Exception e) {
            logger.error("Lỗi khi lấy thông tin ca làm việc", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Lỗi khi lấy thông tin ca làm việc: " + e.getMessage()));
        }
    }

    /**
     * Endpoint dành riêng cho các ứng dụng chung, không yêu cầu phân quyền Manager
     */
    @GetMapping("/common/work-shifts")
    public ResponseEntity<?> getCommonWorkShifts() {
        logger.info("Nhận yêu cầu lấy danh sách ca làm việc chung");

        try {
            // Lấy danh sách tất cả ca làm việc đang active
            List<WorkShift> workShifts = workShiftRepository.findByIsActive(true);

            return ResponseEntity.ok(workShifts);
        } catch (Exception e) {
            logger.error("Lỗi khi lấy danh sách ca làm việc chung", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Lỗi khi lấy danh sách ca làm việc chung: " + e.getMessage()));
        }
    }
}