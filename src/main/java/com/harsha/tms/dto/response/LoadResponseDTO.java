package com.harsha.tms.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.harsha.tms.entity.BookingStatus;
import com.harsha.tms.entity.WeightUnit;

public record LoadResponseDTO(
        UUID id,
        UUID shipperId,
        String pickupLocation,
        String deliveryLocation,
        BigDecimal weight,
        WeightUnit weightUnit,
        String cargoType,
        LocalDateTime pickupDate,
        LocalDateTime deliveryDate,
        BigDecimal offeredPrice,
        Integer trucksRequired,
        Integer remainingTrucks,
        BookingStatus status,
        LocalDateTime datePosted
) {
}
