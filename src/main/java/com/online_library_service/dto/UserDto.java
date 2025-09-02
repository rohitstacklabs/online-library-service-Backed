package com.online_library_service.dto;

import com.online_library_service.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor   
@NoArgsConstructor    
public class UserDto {
    private Long id;

    @NotBlank(message = "Name is required")
    private String name;

    @Email(message = "Email must be valid")
    @NotBlank(message = "Email is required")
    private String email;

    @NotNull(message = "Role is required")
    private Role role;

    @NotNull(message = "Membership start date is required")
    private LocalDate membershipStartDate;

    @NotNull(message = "Membership end date is required")
    private LocalDate membershipEndDate;
}
