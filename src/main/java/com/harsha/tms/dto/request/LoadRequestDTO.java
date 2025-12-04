package com.harsha.tms.dto.request;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record LoadRequestDTO(
        String pickupLocation,
        String deliveryLocation,
        BigDecimal weight,
        String cargoType,
        LocalDateTime pickupDate,
        LocalDateTime deliveryDate,
        BigDecimal offeredPrice,
        Integer trucksRequired
) {
}
