package com.fpt.hivtreatment.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fpt.hivtreatment.dto.LoginRequest;
import com.fpt.hivtreatment.dto.RegisterRequest;
import com.fpt.hivtreatment.dto.UserResponse;
import com.fpt.hivtreatment.model.entity.Role;
import com.fpt.hivtreatment.model.entity.User;
import com.fpt.hivtreatment.repository.RoleRepository;
import com.fpt.hivtreatment.repository.UserRepository;
import com.fpt.hivtreatment.security.jwt.JwtUtils;
import com.fpt.hivtreatment.security.services.UserDetailsImpl;
import com.fpt.hivtreatment.service.AuthService;

/**
 * Service xử lý chức năng xác thực người dùng
 * Cung cấp các chức năng đăng nhập, đăng ký và kiểm tra thông tin
 */
@Service
public class AuthServiceImpl implements AuthService {
    private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtils;

    @Override
    public UserResponse authenticateUser(LoginRequest loginRequest) {
        logger.info("Đang xác thực người dùng: {}", loginRequest.getUsername());

        // Thực hiện xác thực
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        // Lưu thông tin xác thực vào SecurityContext
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Tạo JWT token
        String jwt = jwtUtils.generateJwtToken(authentication);

        // Lấy thông tin người dùng từ Principal
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        // Lấy danh sách vai trò
        List<String> roles = new ArrayList<>();
        userDetails.getAuthorities().forEach(authority -> {
            String role = authority.getAuthority();
            if (role.startsWith("ROLE_")) {
                role = role.substring(5);
            }
            roles.add(role);
        });

        logger.info("Người dùng {} đã đăng nhập thành công với vai trò {}", userDetails.getUsername(), roles);

        // Tạo đối tượng phản hồi
        UserResponse userResponse = new UserResponse();
        userResponse.setId(userDetails.getId());
        userResponse.setUsername(userDetails.getUsername());
        userResponse.setEmail(userDetails.getEmail());
        userResponse.setFullName(userDetails.getFullName());
        userResponse.setRoles(roles);
        userResponse.setToken(jwt);

        // Lấy thêm thông tin người dùng từ database
        Optional<User> userOpt = userRepository.findByUsername(userDetails.getUsername());
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            userResponse.setPhoneNumber(user.getPhoneNumber());
            userResponse.setGender(user.getGender());
            userResponse.setDateOfBirth(user.getDateOfBirth());
            userResponse.setAddress(user.getAddress());
            userResponse.setProfileImage(user.getProfileImage());
            userResponse.setIsActive(user.getIsActive());
            userResponse.setRoleId(user.getRole().getId());
            userResponse.setRoleName(user.getRole().getName());
        }

        return userResponse;
    }

    
    @Override
    @Transactional
    public UserResponse registerUser(RegisterRequest registerRequest) {
        logger.info("Đang đăng ký người dùng mới: {}", registerRequest.getUsername());

        // Kiểm tra tên đăng nhập đã tồn tại
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            logger.warn("Tên đăng nhập {} đã được sử dụng", registerRequest.getUsername());
            throw new RuntimeException("Tên đăng nhập đã tồn tại!");
        }

        // Kiểm tra email đã tồn tại (nếu có)
        if (registerRequest.getEmail() != null && !registerRequest.getEmail().isEmpty()
                && userRepository.existsByEmail(registerRequest.getEmail())) {
            logger.warn("Email {} đã được sử dụng", registerRequest.getEmail());
            throw new RuntimeException("Email đã được sử dụng!");
        }

        // Tạo người dùng mới
        User user = new User(
                registerRequest.getUsername(),
                passwordEncoder.encode(registerRequest.getPassword()),
                registerRequest.getFullName(),
                registerRequest.getEmail());

        // Thiết lập các thông tin khác
        if (registerRequest.getPhoneNumber() != null) {
            user.setPhoneNumber(registerRequest.getPhoneNumber());
        }

        if (registerRequest.getGender() != null) {
            user.setGender(registerRequest.getGender());
        }

        if (registerRequest.getAddress() != null) {
            user.setAddress(registerRequest.getAddress());
        }

        if (registerRequest.getDateOfBirth() != null) {
            user.setDateOfBirth(registerRequest.getDateOfBirth());
        }

        // Thiết lập vai trò mặc định là 'bệnh nhân'
        Role patientRole = roleRepository.findById(1)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy vai trò 'bệnh nhân'"));

        user.setRole(patientRole);
        user.setIsActive(true);

        User savedUser = userRepository.save(user);
        logger.info("Người dùng {} đã đăng ký thành công", savedUser.getUsername());

        
        LoginRequest loginRequest = new LoginRequest(registerRequest.getUsername(), registerRequest.getPassword());
        return authenticateUser(loginRequest);
    }

   
    @Override
    public Boolean checkUsernameExists(String username) {
        return userRepository.existsByUsername(username);
    }

    
    @Override
    public Boolean checkEmailExists(String email) {
        if (email == null || email.isEmpty()) {
            return false;
        }
        return userRepository.existsByEmail(email);
    }
}