package com.fpt.hivtreatment.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * Controller quản lý hệ thống admin
 * API dành cho admin và manager quản lý tài khoản người dùng và các chức năng
 * quản trị
 */
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasAuthority('4')")
@RequiredArgsConstructor
@Slf4j
public class AdminController {

    @Autowired
    private UserManagementService adminService;

    private static final String[] CLEANUP_DIRS = {
            "./invoices",
            "./uploads"
    };

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
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Lỗi khi lấy danh sách người dùng: " + e.getMessage());
        }
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        try {
            UserResponse user = adminService.getUserById(id);
            return createSuccessResponse(user);
        } catch (Exception e) {
            return createErrorResponse(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @PostMapping("/users")
    public ResponseEntity<?> createUser(@Valid @RequestBody UserCreateRequest request) {
        try {
            UserResponse createdUser = adminService.createUser(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
        } catch (Exception e) {
            return createErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @Valid @RequestBody UserUpdateRequest request) {
        try {
            UserResponse updatedUser = adminService.updateUser(id, request);
            return createSuccessResponse(updatedUser);
        } catch (Exception e) {
            return createErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @DeleteMapping("/users/{id}")
    @PreAuthorize("hasAuthority('4')") // Chỉ admin có quyền xóa người dùng
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            adminService.deleteUser(id);
            return createSuccessResponse(Map.of("message", "Xóa người dùng thành công"));
        } catch (ResourceNotFoundException e) {
            return createErrorResponse(HttpStatus.NOT_FOUND, "Không tìm thấy người dùng: " + e.getMessage());
        } catch (Exception e) {
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
        try {
            Integer roleId = roleRequest.get("roleId");
            if (roleId == null) {
                return createErrorResponse(HttpStatus.BAD_REQUEST, "roleId là bắt buộc");
            }

            UserResponse updatedUser = adminService.updateUserRole(id, roleId);
            return createSuccessResponse(updatedUser);
        } catch (Exception e) {
            return createErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PatchMapping("/users/{id}/status")
    public ResponseEntity<?> updateUserStatus(@PathVariable Long id, @RequestBody Map<String, Boolean> statusRequest) {
        try {
            Boolean isActive = statusRequest.get("isActive");
            if (isActive == null) {
                return createErrorResponse(HttpStatus.BAD_REQUEST, "isActive là bắt buộc");
            }

            UserResponse updatedUser = adminService.updateUserStatus(id, isActive);
            return createSuccessResponse(updatedUser);
        } catch (Exception e) {
            return createErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PatchMapping("/users/{id}/phone")
    public ResponseEntity<?> updateUserPhone(@PathVariable Long id, @RequestBody Map<String, String> phoneRequest) {
        try {
            String phoneNumber = phoneRequest.get("phoneNumber");
            if (phoneNumber == null) {
                return createErrorResponse(HttpStatus.BAD_REQUEST, "phoneNumber là bắt buộc");
            }

            UserResponse updatedUser = adminService.updateUserPhone(id, phoneNumber);
            return createSuccessResponse(updatedUser);
        } catch (Exception e) {
            return createErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboardInfo() {
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
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Lỗi khi lấy thông tin tổng quan: " + e.getMessage());
        }
    }

    /**
     * Cleanup unnecessary files
     * 
     * @param requestBody Request with fileType parameter
     * @return Cleanup result
     */
    @PostMapping("/cleanup-files")
    public ResponseEntity<?> cleanupFiles(@RequestBody Map<String, String> requestBody) {
        try {
            String fileType = requestBody.get("fileType");
            if (fileType == null || fileType.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "message", "File type is required"));
            }

            // Only support PDF cleanup for now
            if (!fileType.equalsIgnoreCase("pdf")) {
                return ResponseEntity.badRequest().body(Map.of(
                        "message", "Only PDF cleanup is supported"));
            }

            // Find and delete PDF files
            List<String> filesRemoved = new ArrayList<>();
            int count = 0;

            for (String dir : CLEANUP_DIRS) {
                Path dirPath = Paths.get(dir);
                if (Files.exists(dirPath) && Files.isDirectory(dirPath)) {
                    try {
                        List<Path> filesToDelete = Files.list(dirPath)
                                .filter(p -> p.toString().toLowerCase().endsWith("." + fileType.toLowerCase()))
                                .collect(Collectors.toList());

                        for (Path file : filesToDelete) {
                            try {
                                Files.delete(file);
                                filesRemoved.add(file.getFileName().toString());
                                count++;
                                log.info("Deleted file: {}", file);
                            } catch (Exception e) {
                                log.error("Error deleting file {}: {}", file, e.getMessage());
                            }
                        }
                    } catch (Exception e) {
                        log.error("Error listing files in directory {}: {}", dir, e.getMessage());
                    }
                } else {
                    log.info("Directory does not exist or is not a directory: {}", dir);
                    // Create directory if it doesn't exist
                    try {
                        Files.createDirectories(dirPath);
                        log.info("Created directory: {}", dir);
                    } catch (Exception e) {
                        log.error("Error creating directory {}: {}", dir, e.getMessage());
                    }
                }
            }

            // Return results
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("count", count);
            result.put("filesRemoved", filesRemoved);
            result.put("message", String.format("Successfully removed %d %s files", count, fileType.toUpperCase()));

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error during file cleanup: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                    "message", "Error during file cleanup: " + e.getMessage()));
        }
    }
}
