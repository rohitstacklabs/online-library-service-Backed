
package com.online_library_service.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MembershipDto {
    @Min(value = 1, message = "Months must be at least 1")
    @NotNull(message = "Months is required")
    private Integer months;
}