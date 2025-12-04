package com.harsha.tms.dto.request;

import java.util.UUID;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record BidRequestDTO(
        @NotNull(message = "Load ID is required")
        UUID loadId,
        
        @NotNull(message = "Transporter ID is required")
        UUID transporterId,
        
        @NotNull(message = "Proposed rate is required")
        @Positive(message = "Proposed rate must be positive")
        Double proposedRate,
        
        @NotNull(message = "Trucks offered is required")
        @Min(value = 1, message = "At least 1 truck must be offered")
        Integer trucksOffered,
        
        @NotBlank(message = "Truck type is required")
        String truckType
) {
}

