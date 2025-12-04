package com.harsha.tms.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.harsha.tms.entity.BookingStatus;

public record LoadResponseDTO(
        UUID id,
        String pickupLocation,
        String deliveryLocation,
        BigDecimal weight,
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
