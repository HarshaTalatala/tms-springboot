package com.harsha.tms.dto.request;

import java.util.UUID;

public record BidRequestDTO(
        UUID loadId,
        UUID transporterId,
        Double proposedRate,
        Integer trucksOffered,
        String truckType
) {
}

