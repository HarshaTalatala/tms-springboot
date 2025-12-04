package com.harsha.tms.dto.request;

import java.util.UUID;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record BookingRequestDTO(
        @NotNull(message = "Load ID is required")
        UUID loadId,
        
        @NotNull(message = "Bid ID is required")
        UUID bidId,
        
        @NotNull(message = "Transporter ID is required")
        UUID transporterId,
        
        @NotNull(message = "Allocated trucks is required")
        @Min(value = 1, message = "At least 1 truck must be allocated")
        Integer allocatedTrucks,
        
        @NotNull(message = "Final rate is required")
        @Positive(message = "Final rate must be positive")
        Double finalRate
) {
}

