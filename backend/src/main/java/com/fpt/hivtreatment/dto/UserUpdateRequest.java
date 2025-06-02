package com.fpt.hivtreatment.dto;

import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateRequest {

    private String fullName;

    @Email(message = "Email should be valid")
    private String email;

    private String phone;

    private String address;

    private Boolean isActive;

    private Integer roleId; // Thêm để có thể update role
}