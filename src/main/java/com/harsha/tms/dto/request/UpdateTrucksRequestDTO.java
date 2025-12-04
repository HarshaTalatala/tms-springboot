package com.harsha.tms.dto.request;

import java.util.List;

public record UpdateTrucksRequestDTO(
        List<TruckDTO> trucks
) {
    public record TruckDTO(
            String truckType,
            Integer count
    ) {
    }
}
