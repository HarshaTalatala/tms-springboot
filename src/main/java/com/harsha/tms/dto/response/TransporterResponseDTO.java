package com.harsha.tms.dto.response;

import java.util.UUID;

public record TransporterResponseDTO(
        UUID transporterId,
        String companyName,
        Double rating
) {
}

