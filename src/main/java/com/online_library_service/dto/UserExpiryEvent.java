package com.online_library_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserExpiryEvent {
    private Long userId;
    private String email;
    private String name;
    private LocalDate membershipEndDate;
    private String reason;
}
