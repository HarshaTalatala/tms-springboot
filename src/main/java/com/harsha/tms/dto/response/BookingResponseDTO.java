package com.harsha.tms.dto.response;

import com.harsha.tms.entity.BookingStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record BookingResponseDTO(
        UUID bookingId,
        UUID loadId,
        UUID bidId,
        UUID transporterId,
        Integer allocatedTrucks,
        Double finalRate,
        BookingStatus status,
        LocalDateTime bookedAt
) {
}

