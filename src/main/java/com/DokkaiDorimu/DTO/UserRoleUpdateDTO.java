package com.DokkaiDorimu.DTO;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRoleUpdateDTO {
    private Long id;
    private String username;
    private String role;
    private String email;
    private String imgUrl;
    private String status;
    // Add any other necessary fields that don't cause circular references
}

