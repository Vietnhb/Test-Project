package com.fpt.hivtreatment.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fpt.hivtreatment.dto.LoginRequest;
import com.fpt.hivtreatment.dto.RegisterRequest;
import com.fpt.hivtreatment.dto.UserResponse;
import com.fpt.hivtreatment.security.services.UserDetailsImpl;
import com.fpt.hivtreatment.service.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * Controller xử lý các chức năng xác thực như đăng nhập, đăng ký
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "API xác thực người dùng")
public class AuthController {
    @Autowired
    private AuthService authService;

    @Operation(summary = "Kiểm tra trạng thái hoạt động", description = "Endpoint kiểm tra xem dịch vụ xác thực có hoạt động hay không")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dịch vụ hoạt động bình thường", content = @Content(mediaType = "application/json"))
    })
    @GetMapping("/health")
    public ResponseEntity<?> healthCheck() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("message", "Auth service is running");
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint để kiểm tra xác thực và lấy thông tin người dùng hiện tại
     * Yêu cầu phải có JWT token hợp lệ
     * 
     * @return Thông tin của người dùng đang đăng nhập
     */
    @Operation(summary = "Lấy thông tin người dùng hiện tại", description = "Trả về thông tin người dùng đã đăng nhập dựa trên JWT token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lấy thông tin thành công", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực hoặc token không hợp lệ", content = @Content(mediaType = "application/json"))
    })
    @GetMapping("/user")
    public ResponseEntity<?> getCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication != null && authentication.isAuthenticated() &&
                    authentication.getPrincipal() instanceof UserDetailsImpl) {

                UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

                Map<String, Object> response = new HashMap<>();
                response.put("id", userDetails.getId());
                response.put("username", userDetails.getUsername());
                response.put("email", userDetails.getEmail());
                response.put("fullName", userDetails.getFullName());
                response.put("authorities", userDetails.getAuthorities());
                response.put("authenticated", true);

                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(errorResponse("User not authenticated"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse("Error retrieving current user: " + e.getMessage()));
        }
    }

    @Operation(summary = "Đăng nhập", description = "Đăng nhập vào hệ thống và nhận JWT token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Đăng nhập thành công", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "401", description = "Sai tên đăng nhập hoặc mật khẩu", content = @Content(mediaType = "application/json"))
    })
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            UserResponse userResponse = authService.authenticateUser(loginRequest);
            return ResponseEntity.ok(userResponse);
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(errorResponse("Invalid username or password"));
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(errorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse("An error occurred during login: " + e.getMessage()));
        }
    }

    @Operation(summary = "Đăng ký", description = "Đăng ký tài khoản mới vào hệ thống")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Đăng ký thành công", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ hoặc người dùng đã tồn tại", content = @Content(mediaType = "application/json"))
    })
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        try {
            UserResponse userResponse = authService.registerUser(registerRequest);
            return ResponseEntity.ok(userResponse);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(errorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse("An error occurred during registration: " + e.getMessage()));
        }
    }

    @Operation(summary = "Kiểm tra tên đăng nhập", description = "Kiểm tra xem tên đăng nhập đã tồn tại trong hệ thống chưa")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Kiểm tra thành công", content = @Content(mediaType = "application/json"))
    })
    @GetMapping("/check-username/{username}")
    public ResponseEntity<?> checkUsernameExists(@PathVariable String username) {
        try {
            Boolean exists = authService.checkUsernameExists(username);
            Map<String, Boolean> response = new HashMap<>();
            response.put("exists", exists);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse("Error checking username: " + e.getMessage()));
        }
    }

    @Operation(summary = "Kiểm tra email", description = "Kiểm tra xem email đã tồn tại trong hệ thống chưa")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Kiểm tra thành công", content = @Content(mediaType = "application/json"))
    })
    @GetMapping("/check-email/{email}")
    public ResponseEntity<?> checkEmailExists(@PathVariable String email) {
        try {
            Boolean exists = authService.checkEmailExists(email);
            Map<String, Boolean> response = new HashMap<>();
            response.put("exists", exists);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse("Error checking email: " + e.getMessage()));
        }
    }

    private Map<String, String> errorResponse(String message) {
        Map<String, String> errorMap = new HashMap<>();
        errorMap.put("message", message);
        return errorMap;
    }
}