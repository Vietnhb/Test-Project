package com.fpt.hivtreatment.controller;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fpt.hivtreatment.security.services.UserDetailsImpl;

/**
 * Controller xử lý các chức năng liên quan đến người dùng
 */
@RestController
@RequestMapping("/api/user")
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @GetMapping("/me")
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
            logger.error("Error retrieving current user: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse("Error retrieving user information: " + e.getMessage()));
        }
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('4')")
    public ResponseEntity<?> adminAccess() {
        return ResponseEntity.ok(Map.of("message", "Admin Content"));
    }

    @GetMapping("/doctor")
    @PreAuthorize("hasRole('2')")
    public ResponseEntity<?> doctorAccess() {
        return ResponseEntity.ok(Map.of("message", "Doctor Content"));
    }

    @GetMapping("/staff")
    @PreAuthorize("hasRole('3')")
    public ResponseEntity<?> staffAccess() {
        return ResponseEntity.ok(Map.of("message", "Staff Content"));
    }

    @GetMapping("/patient")
    @PreAuthorize("hasRole('1')")
    public ResponseEntity<?> patientAccess() {
        return ResponseEntity.ok(Map.of("message", "Patient Content"));
    }

    @GetMapping("/manager")
    @PreAuthorize("hasRole('5')")
    public ResponseEntity<?> managerAccess() {
        return ResponseEntity.ok(Map.of("message", "Manager Content"));
    }

    private Map<String, String> errorResponse(String message) {
        Map<String, String> errorMap = new HashMap<>();
        errorMap.put("message", message);
        return errorMap;
    }
}