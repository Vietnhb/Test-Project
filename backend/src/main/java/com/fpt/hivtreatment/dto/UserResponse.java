package com.fpt.hivtreatment.dto;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private Long id;
    private String username;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String address;
    private Integer roleId;
    private String roleName;
    private List<String> roles;
    private String token;
    private String gender;
    private Date dateOfBirth;
    private String profileImage;
    private Boolean isActive;
    private LocalDateTime createdAt;
}