package com.fpt.hivtreatment.service;

import java.util.List;
import java.util.Map;

import com.fpt.hivtreatment.dto.UserCreateRequest;
import com.fpt.hivtreatment.dto.UserResponse;
import com.fpt.hivtreatment.dto.UserUpdateRequest;

public interface UserManagementService {

    List<UserResponse> getAllUsers(Map<String, Object> filters, int page, int size);

    long countUsers(Map<String, Object> filters);

    UserResponse getUserById(Long id);

    UserResponse createUser(UserCreateRequest request);

    UserResponse updateUser(Long id, UserUpdateRequest request);

    UserResponse updateUserRole(Long id, Integer roleId);

    UserResponse updateUserStatus(Long id, Boolean isActive);

    UserResponse updateUserPhone(Long id, String phoneNumber);

    void deleteUser(Long id);

    void resetPassword(Long id, String newPassword);
}