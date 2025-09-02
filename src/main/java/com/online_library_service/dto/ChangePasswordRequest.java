package com.online_library_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChangePasswordRequest {

    @Email(message = "Email must be valid")
    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "Old password is required")
    private String password;  

    @NotBlank(message = "New password is required")
    private String newPassword;

    public String getOldPassword() {
        return password;
    }
}
