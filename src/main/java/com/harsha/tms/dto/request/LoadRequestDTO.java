package com.harsha.tms.dto.request;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.harsha.tms.entity.WeightUnit;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record LoadRequestDTO(
        @NotNull(message = "Shipper ID is required")
        UUID shipperId,
        
        @NotBlank(message = "Pickup location is required")
        String pickupLocation,
        
        @NotBlank(message = "Delivery location is required")
        String deliveryLocation,
        
        @NotNull(message = "Weight is required")
        @Positive(message = "Weight must be positive")
        BigDecimal weight,
        
        @NotNull(message = "Weight unit is required")
        WeightUnit weightUnit,
        
        @NotBlank(message = "Cargo type is required")
        String cargoType,
        
        @NotNull(message = "Pickup date is required")
        @Future(message = "Pickup date must be in the future")
        LocalDateTime pickupDate,
        
        @NotNull(message = "Delivery date is required")
        @Future(message = "Delivery date must be in the future")
        LocalDateTime deliveryDate,
        
        @NotNull(message = "Offered price is required")
        @Positive(message = "Offered price must be positive")
        BigDecimal offeredPrice,
        
        @NotNull(message = "Trucks required is required")
        @Min(value = 1, message = "At least 1 truck is required")
        Integer trucksRequired
) {
}
