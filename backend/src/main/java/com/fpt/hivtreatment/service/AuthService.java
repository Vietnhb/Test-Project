package com.fpt.hivtreatment.service;

import com.fpt.hivtreatment.dto.LoginRequest;
import com.fpt.hivtreatment.dto.RegisterRequest;
import com.fpt.hivtreatment.dto.UserResponse;

public interface AuthService {
    
    UserResponse authenticateUser(LoginRequest loginRequest);

    UserResponse registerUser(RegisterRequest registerRequest);

    Boolean checkUsernameExists(String username);

    Boolean checkEmailExists(String email);
}