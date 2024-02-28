package com.cinar.authentication.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateUserRequest {
    private String updatedBy;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
}
