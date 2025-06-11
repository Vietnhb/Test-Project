package com.fpt.hivtreatment.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fpt.hivtreatment.dto.UserResponse;
import com.fpt.hivtreatment.service.UserManagementService;
import com.fpt.hivtreatment.security.services.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Controller xử lý các chức năng liên quan đến người dùng
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserManagementService userManagementService;

    @GetMapping("/user/me")
    public ResponseEntity<?> getCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || !authentication.isAuthenticated() ||
                    "anonymousUser".equals(authentication.getPrincipal())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(errorResponse("User not authenticated"));
            }

            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

            Map<String, Object> response = new HashMap<>();
            response.put("id", userDetails.getId());
            response.put("username", userDetails.getUsername());
            response.put("email", userDetails.getEmail());
            response.put("fullName", userDetails.getFullName());
            response.put("authorities", userDetails.getAuthorities());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse("Error retrieving user information: " + e.getMessage()));
        }
    }

<<<<<<< HEAD
    @GetMapping("/users/profile")
    public ResponseEntity<?> getUserProfile() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || !authentication.isAuthenticated() ||
                    "anonymousUser".equals(authentication.getPrincipal())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(errorResponse("User not authenticated"));
            }

            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            Long userId = userDetails.getId();

            log.info("Fetching profile for user ID: {}", userId);
            UserResponse user = userManagementService.getUserById(userId);

            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(errorResponse("User profile not found"));
            }

            return ResponseEntity.ok(user);
        } catch (Exception e) {
            log.error("Error fetching user profile: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse("Error retrieving user profile: " + e.getMessage()));
        }
    }

    // New endpoint to get user by ID - accessible to staff and admins
    @GetMapping("/users/{id}")
    @PreAuthorize("hasAnyAuthority('3', '4','2', '5')") // Staff, Admin, Manager
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        try {
            log.info("Fetching user with ID: {}", id);
            UserResponse user = userManagementService.getUserById(id);

            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(errorResponse("User not found with ID: " + id));
            }

            return ResponseEntity.ok(user);
        } catch (Exception e) {
            log.error("Error fetching user with ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse("Error retrieving user information: " + e.getMessage()));
        }
    }

    // New endpoint to get all patients - accessible to staff
    @GetMapping("/patients")
    @PreAuthorize("hasAnyAuthority('2', '3', '4', '5')") // Doctor, Staff, Admin, Manager
    public ResponseEntity<?> getAllPatients() {
        try {
            log.info("Fetching all patients");
            // Filter users with role 1 (patients)
            Map<String, Object> filters = new HashMap<>();
            filters.put("roleId", 1);

            List<UserResponse> patients = userManagementService.getAllUsers(filters, 0, 1000);

            return ResponseEntity.ok(patients);
        } catch (Exception e) {
            log.error("Error fetching patients: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse("Error retrieving patients: " + e.getMessage()));
        }
    }

    @GetMapping("/user/admin")
=======
    @GetMapping("/admin")
>>>>>>> fd42c148e0431975301ca683137e9cc7dea64a1c
    @PreAuthorize("hasAuthority('4')")
    public ResponseEntity<?> adminAccess() {
        return ResponseEntity.ok(Map.of("message", "Admin Content"));
    }

<<<<<<< HEAD
    @GetMapping("/user/doctor")
=======
    @GetMapping("/doctor")
>>>>>>> fd42c148e0431975301ca683137e9cc7dea64a1c
    @PreAuthorize("hasAuthority('2')")
    public ResponseEntity<?> doctorAccess() {
        return ResponseEntity.ok(Map.of("message", "Doctor Content"));
    }

<<<<<<< HEAD
    @GetMapping("/user/staff")
=======
    @GetMapping("/staff")
>>>>>>> fd42c148e0431975301ca683137e9cc7dea64a1c
    @PreAuthorize("hasAuthority('3')")
    public ResponseEntity<?> staffAccess() {
        return ResponseEntity.ok(Map.of("message", "Staff Content"));
    }

<<<<<<< HEAD
    @GetMapping("/user/patient")
=======
    @GetMapping("/patient")
>>>>>>> fd42c148e0431975301ca683137e9cc7dea64a1c
    @PreAuthorize("hasAuthority('1')")
    public ResponseEntity<?> patientAccess() {
        return ResponseEntity.ok(Map.of("message", "Patient Content"));
    }

<<<<<<< HEAD
    @GetMapping("/user/manager")
=======
    @GetMapping("/manager")
>>>>>>> fd42c148e0431975301ca683137e9cc7dea64a1c
    @PreAuthorize("hasAuthority('5')")
    public ResponseEntity<?> managerAccess() {
        return ResponseEntity.ok(Map.of("message", "Manager Content"));
    }

    private Map<String, String> errorResponse(String message) {
        Map<String, String> errorMap = new HashMap<>();
        errorMap.put("message", message);
        return errorMap;
    }
}