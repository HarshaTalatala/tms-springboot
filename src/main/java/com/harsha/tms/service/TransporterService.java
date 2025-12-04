package com.harsha.tms.service;

import java.util.UUID;

import com.harsha.tms.dto.request.TransporterRequestDTO;
import com.harsha.tms.dto.request.UpdateTrucksRequestDTO;
import com.harsha.tms.dto.response.TransporterResponseDTO;

public interface TransporterService {

    TransporterResponseDTO createTransporter(TransporterRequestDTO request);

    TransporterResponseDTO getTransporterById(UUID transporterId);

    TransporterResponseDTO updateTrucks(UUID transporterId, UpdateTrucksRequestDTO request);
}
