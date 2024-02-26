package com.cinar.authentication.dto;

import com.cinar.authentication.model.Role;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDto {
    private String username;
    @Column(nullable = false)
    private String password;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private List<Role> authorities;
}
