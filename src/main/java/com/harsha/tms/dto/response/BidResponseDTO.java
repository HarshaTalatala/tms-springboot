package com.harsha.tms.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import com.harsha.tms.entity.BidStatus;

public record BidResponseDTO(
        UUID bidId,
        UUID loadId,
        UUID transporterId,
        Double proposedRate,
        Integer trucksOffered,
        String truckType,
        BidStatus status,
        LocalDateTime submittedAt
) {
}

