package com.harsha.tms.dto.request;

import java.util.UUID;

public record BookingRequestDTO(
        UUID loadId,
        UUID bidId,
        UUID transporterId,
        Integer allocatedTrucks,
        Double finalRate
) {
}

