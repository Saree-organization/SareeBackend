package com.web.saree.dto.response;

import com.web.saree.entity.Users;
import lombok.Data;

@Data
public class UserResponse {

    private Long id;
    private String email;
    private String role; // ✨ ADDED: Role field

    // constructor to map entity to DTO
    public UserResponse(Users user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.role = user.getRole(); // ✨ ADDED: Map role from entity

    }
}
