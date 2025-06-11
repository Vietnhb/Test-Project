package com.fpt.hivtreatment.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fpt.hivtreatment.dto.UserCreateRequest;
import com.fpt.hivtreatment.dto.UserResponse;
import com.fpt.hivtreatment.dto.UserUpdateRequest;
import com.fpt.hivtreatment.model.entity.Role;
import com.fpt.hivtreatment.model.entity.User;
import com.fpt.hivtreatment.exception.ResourceNotFoundException;
import com.fpt.hivtreatment.repository.RoleRepository;
import com.fpt.hivtreatment.repository.UserRepository;
import com.fpt.hivtreatment.service.UserManagementService;

@Service
public class UserManagementServiceImpl implements UserManagementService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder; // Phương thức trích xuất logic xử lý filters chung

    private FilterCriteria extractFilterCriteria(Map<String, Object> filters) {
        Integer roleId = null;

        // Check both roleId and role keys to support both frontend implementations
        if (filters.get("roleId") != null) {
            roleId = Integer.parseInt(filters.get("roleId").toString());
        } else if (filters.get("role") != null) {
            roleId = Integer.parseInt(filters.get("role").toString());
        }

        Boolean isActive = filters.get("isActive") != null ? (Boolean) filters.get("isActive") : null;
        String search = filters.get("search") != null ? filters.get("search").toString() : null;

        return new FilterCriteria(roleId, isActive, search);
    }

    // Phương thức áp dụng search filter
    private boolean matchesSearchCriteria(User user, String search) {
        if (search == null || search.isEmpty()) {
            return true;
        }
        return user.getUsername().contains(search) ||
                (user.getFullName() != null && user.getFullName().contains(search)) ||
                (user.getEmail() != null && user.getEmail().contains(search));
    }

    @Override
    public List<UserResponse> getAllUsers(Map<String, Object> filters, int page, int size) {
        List<User> users;

        if (filters.isEmpty()) {
            users = userRepository.findAll(PageRequest.of(page, size)).getContent();
        } else {
            FilterCriteria criteria = extractFilterCriteria(filters);

            // Query database based on roleId and isActive
            if (criteria.roleId != null && criteria.isActive != null) {
                users = userRepository.findByRoleIdAndIsActive(criteria.roleId, criteria.isActive,
                        PageRequest.of(page, size));
            } else if (criteria.roleId != null) {
                users = userRepository.findByRoleId(criteria.roleId, PageRequest.of(page, size));
            } else if (criteria.isActive != null) {
                users = userRepository.findByIsActive(criteria.isActive, PageRequest.of(page, size));
            } else {
                users = userRepository.findAll(PageRequest.of(page, size)).getContent();
            }

            // Apply search filter in memory
            if (criteria.search != null && !criteria.search.isEmpty()) {
                users = users.stream()
                        .filter(u -> matchesSearchCriteria(u, criteria.search))
                        .toList();
            }
        }

        // Convert to DTOs using stream for cleaner code
        return users.stream()
                .map(this::mapUserToResponse)
                .toList();
    }

    @Override
    public long countUsers(Map<String, Object> filters) {
        if (filters.isEmpty()) {
            return userRepository.count();
        }

        FilterCriteria criteria = extractFilterCriteria(filters);
        long count;

        // Count based on roleId and isActive
        if (criteria.roleId != null && criteria.isActive != null) {
            count = userRepository.countByRoleIdAndIsActive(criteria.roleId, criteria.isActive);
        } else if (criteria.roleId != null) {
            count = userRepository.countByRoleId(criteria.roleId);
        } else if (criteria.isActive != null) {
            count = userRepository.countByIsActive(criteria.isActive);
        } else {
            count = userRepository.count();
        }

        // For search, apply filter manually (ideally should be a DB query)
        if (criteria.search != null && !criteria.search.isEmpty()) {
            List<User> allUsers = userRepository.findAll();
            count = allUsers.stream()
                    .filter(u -> matchesSearchCriteria(u, criteria.search))
                    .count();
        }

        return count;
    }

    // Inner class for filter criteria
    private static class FilterCriteria {
        final Integer roleId;
        final Boolean isActive;
        final String search;

        FilterCriteria(Integer roleId, Boolean isActive, String search) {
            this.roleId = roleId;
            this.isActive = isActive;
            this.search = search;
        }
    }

    @Override
    public UserResponse getUserById(Long id) {
        User user = findUserById(id);
        return mapUserToResponse(user);
    }

    /**
     * Tạo người dùng mới
     */
    @Override
    @Transactional
    public UserResponse createUser(UserCreateRequest request) {
        // Kiểm tra username đã tồn tại chưa
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Tên đăng nhập đã tồn tại: " + request.getUsername());
        }

        // Xác thực vai trò
        Role role = roleRepository.findById(request.getRoleId())
                .orElseThrow(
                        () -> new ResourceNotFoundException("Không tìm thấy vai trò với id: " + request.getRoleId()));

        // Tạo người dùng
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPhoneNumber(request.getPhone());
        user.setAddress(request.getAddress());
        user.setRole(role);
        user.setIsActive(request.isActive());

        User savedUser = userRepository.save(user);

        return mapUserToResponse(savedUser);
    }

    @Override
    @Transactional
    public UserResponse updateUser(Long id, UserUpdateRequest request) {
        User user = findUserById(id);

        // Update fields only if provided
        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }
        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }
        if (request.getPhone() != null) {
            user.setPhoneNumber(request.getPhone());
        }
        if (request.getAddress() != null) {
            user.setAddress(request.getAddress());
        }
        if (request.getIsActive() != null) {
            user.setIsActive(request.getIsActive());
        }
        if (request.getRoleId() != null) {
            Role role = roleRepository.findById(request.getRoleId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Không tìm thấy vai trò với id: " + request.getRoleId()));
            user.setRole(role);
        }

        User updatedUser = userRepository.save(user);
        return mapUserToResponse(updatedUser);
    }

    // Phương thức tìm user chung
    private User findUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với id: " + id));
    }

    // Convenience methods for backward compatibility và các API đặc biệt
    @Override
    @Transactional
    public UserResponse updateUserRole(Long id, Integer roleId) {
        UserUpdateRequest request = new UserUpdateRequest();
        request.setRoleId(roleId);
        return updateUser(id, request);
    }

    @Override
    @Transactional
    public UserResponse updateUserStatus(Long id, Boolean isActive) {
        UserUpdateRequest request = new UserUpdateRequest();
        request.setIsActive(isActive);
        return updateUser(id, request);
    }

    @Override
    @Transactional
    public UserResponse updateUserPhone(Long id, String phoneNumber) {
        UserUpdateRequest request = new UserUpdateRequest();
        request.setPhone(phoneNumber);
        return updateUser(id, request);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        findUserById(id); // Validate user exists first

        int deleted = userRepository.deleteUserById(id);
        if (deleted == 0) {
            throw new RuntimeException("Không thể xóa người dùng với ID: " + id);
        }
    }

    @Override
    @Transactional
    public void resetPassword(Long id, String newPassword) {
        User user = findUserById(id);
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    /**
     * Phương thức hỗ trợ chuyển đổi từ entity User sang DTO UserResponse
     */
    private UserResponse mapUserToResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setFullName(user.getFullName());
        response.setEmail(user.getEmail());
        response.setPhoneNumber(user.getPhoneNumber());
        response.setAddress(user.getAddress());
        response.setRoleId(user.getRole().getId());
        response.setRoleName(user.getRole().getName());
        response.setIsActive(user.getIsActive());
        response.setCreatedAt(java.time.LocalDateTime.now());

        // Bổ sung các trường còn thiếu
        response.setGender(user.getGender());
        response.setDateOfBirth(user.getDateOfBirth());
        response.setProfileImage(user.getProfileImage());

        return response;
    }
}