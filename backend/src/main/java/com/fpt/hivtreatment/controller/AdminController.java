package com.fpt.hivtreatment.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.fpt.hivtreatment.dto.UserCreateRequest;
import com.fpt.hivtreatment.dto.UserResponse;
import com.fpt.hivtreatment.dto.UserUpdateRequest;
import com.fpt.hivtreatment.service.UserManagementService;
import com.fpt.hivtreatment.exception.ResourceNotFoundException;

import jakarta.validation.Valid;

/**
 * Controller quản lý hệ thống admin
 * API dành cho admin và manager quản lý tài khoản người dùng và các chức năng
 * quản trị
 */
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasAnyAuthority('4', '5')") // Admin (role_id = 4) hoặc Manager (role_id = 5) có quyền truy cập
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    @Autowired
    private UserManagementService adminService;


    private ResponseEntity<?> createErrorResponse(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(Map.of("message", message));
    }

    // Phương thức tạo response chung cho thành công
    private ResponseEntity<?> createSuccessResponse(Object data) {
        return ResponseEntity.ok(data);
    }

    /**
     * Lấy danh sách người dùng với bộ lọc và phân trang
     */
    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers(
            @RequestParam(required = false) String roleId,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        logger.info("Lấy danh sách người dùng - roleId: {}, isActive: {}, search: {}, page: {}, size: {}",
                roleId, isActive, search, page, size);

        try {
            // Build filters
            Map<String, Object> filters = new HashMap<>();
            if (roleId != null && !roleId.isEmpty())
                filters.put("roleId", roleId);
            if (isActive != null)
                filters.put("isActive", isActive);
            if (search != null && !search.isEmpty())
                filters.put("search", search);

            // Get data
            List<UserResponse> users = adminService.getAllUsers(filters, page, size);
            long total = adminService.countUsers(filters);

            // Build response
            Map<String, Object> response = Map.of(
                    "users", users,
                    "totalItems", total,
                    "currentPage", page,
                    "totalPages", Math.ceil((double) total / size));

            return createSuccessResponse(response);
        } catch (Exception e) {
            logger.error("Lỗi khi lấy danh sách người dùng", e);
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Lỗi khi lấy danh sách người dùng: " + e.getMessage());
        }
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        logger.info("Lấy thông tin người dùng với id: {}", id);

        try {
            UserResponse user = adminService.getUserById(id);
            return createSuccessResponse(user);
        } catch (Exception e) {
            logger.error("Lỗi khi lấy thông tin người dùng với id: " + id, e);
            return createErrorResponse(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @PostMapping("/users")
    public ResponseEntity<?> createUser(@Valid @RequestBody UserCreateRequest request) {
        logger.info("Tạo người dùng mới với tên đăng nhập: {}", request.getUsername());

        try {
            UserResponse createdUser = adminService.createUser(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
        } catch (Exception e) {
            logger.error("Lỗi khi tạo người dùng mới", e);
            return createErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @Valid @RequestBody UserUpdateRequest request) {
        logger.info("Cập nhật thông tin người dùng với id: {}", id);

        try {
            UserResponse updatedUser = adminService.updateUser(id, request);
            return createSuccessResponse(updatedUser);
        } catch (Exception e) {
            logger.error("Lỗi khi cập nhật thông tin người dùng với id: " + id, e);
            return createErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @DeleteMapping("/users/{id}")
    @PreAuthorize("hasAuthority('4')") // Chỉ admin có quyền xóa người dùng
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        logger.info("Xóa người dùng với ID: {}", id);

        try {
            adminService.deleteUser(id);
            return createSuccessResponse(Map.of("message", "Xóa người dùng thành công"));
        } catch (ResourceNotFoundException e) {
            logger.error("Không tìm thấy người dùng để xóa: {}", e.getMessage());
            return createErrorResponse(HttpStatus.NOT_FOUND, "Không tìm thấy người dùng: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Lỗi khi xóa người dùng: {}", e.getMessage(), e);
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Lỗi khi xóa người dùng: " + e.getMessage());
        }
    }

    // Note: Các PATCH endpoints riêng biệt được giữ lại để:
    // 1. Backward compatibility với frontend hiện tại
    // 2. Phân quyền đặc biệt (VD: chỉ admin mới được update role)
    // 3. Validation riêng cho từng field (VD: phone format)
    // Frontend có thể sử dụng PUT /users/{id} với UserUpdateRequest đầy đủ
    // hoặc các PATCH endpoints riêng để update từng field cụ thể

    @PatchMapping("/users/{id}/role")
    @PreAuthorize("hasAuthority('4')") // Chỉ admin mới có quyền thay đổi vai trò
    public ResponseEntity<?> updateUserRole(@PathVariable Long id, @RequestBody Map<String, Integer> roleRequest) {
        logger.info("Cập nhật vai trò người dùng với id: {} và roleId: {}", id, roleRequest.get("roleId"));

        try {
            Integer roleId = roleRequest.get("roleId");
            if (roleId == null) {
                return createErrorResponse(HttpStatus.BAD_REQUEST, "roleId là bắt buộc");
            }

            UserResponse updatedUser = adminService.updateUserRole(id, roleId);
            return createSuccessResponse(updatedUser);
        } catch (Exception e) {
            logger.error("Lỗi khi cập nhật vai trò người dùng với id: " + id, e);
            return createErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PatchMapping("/users/{id}/status")
    public ResponseEntity<?> updateUserStatus(@PathVariable Long id, @RequestBody Map<String, Boolean> statusRequest) {
        logger.info("Cập nhật trạng thái người dùng với id: {} và status: {}", id, statusRequest.get("isActive"));

        try {
            Boolean isActive = statusRequest.get("isActive");
            if (isActive == null) {
                return createErrorResponse(HttpStatus.BAD_REQUEST, "isActive là bắt buộc");
            }

            UserResponse updatedUser = adminService.updateUserStatus(id, isActive);
            return createSuccessResponse(updatedUser);
        } catch (Exception e) {
            logger.error("Lỗi khi cập nhật trạng thái người dùng với id: " + id, e);
            return createErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PatchMapping("/users/{id}/phone")
    public ResponseEntity<?> updateUserPhone(@PathVariable Long id, @RequestBody Map<String, String> phoneRequest) {
        logger.info("Cập nhật số điện thoại người dùng với id: {} và số điện thoại: {}",
                id, phoneRequest.get("phoneNumber"));

        try {
            String phoneNumber = phoneRequest.get("phoneNumber");
            if (phoneNumber == null) {
                return createErrorResponse(HttpStatus.BAD_REQUEST, "phoneNumber là bắt buộc");
            }

            UserResponse updatedUser = adminService.updateUserPhone(id, phoneNumber);
            return createSuccessResponse(updatedUser);
        } catch (Exception e) {
            logger.error("Lỗi khi cập nhật số điện thoại người dùng với id: " + id, e);
            return createErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboardInfo() {
        logger.info("Lấy thông tin tổng quan cho dashboard admin");

        try {
            // Build dashboard info with role counts
            Map<String, Object> dashboardInfo = Map.of(
                    "totalUsers", adminService.countUsers(new HashMap<>()),
                    "totalPatients", adminService.countUsers(Map.of("roleId", "1")),
                    "totalDoctors", adminService.countUsers(Map.of("roleId", "2")),
                    "totalStaff", adminService.countUsers(Map.of("roleId", "3")),
                    "totalAdmins", adminService.countUsers(Map.of("roleId", "4")),
                    "totalManagers", adminService.countUsers(Map.of("roleId", "5")));

            return createSuccessResponse(dashboardInfo);
        } catch (Exception e) {
            logger.error("Lỗi khi lấy thông tin tổng quan cho dashboard admin", e);
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Lỗi khi lấy thông tin tổng quan: " + e.getMessage());
        }
    }}
