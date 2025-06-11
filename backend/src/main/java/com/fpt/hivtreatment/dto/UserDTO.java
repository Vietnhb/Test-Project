package com.fpt.hivtreatment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Long id;
    private String username;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String profileImage;
    private Integer roleId;
    private String roleName;

    // Add missing fields from User entity
    private Date dateOfBirth;
    private String gender;
    private String address;
    private Boolean isActive;
}