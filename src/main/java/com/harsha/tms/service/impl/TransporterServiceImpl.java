package com.harsha.tms.service.impl;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.harsha.tms.dto.request.TransporterRequestDTO;
import com.harsha.tms.dto.request.UpdateTrucksRequestDTO;
import com.harsha.tms.dto.response.TransporterResponseDTO;
import com.harsha.tms.entity.Transporter;
import com.harsha.tms.entity.Truck;
import com.harsha.tms.exception.ResourceNotFoundException;
import com.harsha.tms.repository.TransporterRepository;
import com.harsha.tms.repository.TruckRepository;
import com.harsha.tms.service.TransporterService;

@Service
public class TransporterServiceImpl implements TransporterService {

    private final TransporterRepository transporterRepository;
    private final TruckRepository truckRepository;

    public TransporterServiceImpl(TransporterRepository transporterRepository, TruckRepository truckRepository) {
        this.transporterRepository = transporterRepository;
        this.truckRepository = truckRepository;
    }

    @Override
    public TransporterResponseDTO createTransporter(TransporterRequestDTO request) {
        Transporter transporter = new Transporter();
        transporter.setCompanyName(request.companyName());
        transporter.setRating(request.rating());

        Transporter savedTransporter = transporterRepository.save(transporter);

        return new TransporterResponseDTO(
                savedTransporter.getTransporterId(),
                savedTransporter.getCompanyName(),
                savedTransporter.getRating()
        );
    }

    @Override
    public TransporterResponseDTO getTransporterById(UUID transporterId) {
        Transporter transporter = transporterRepository.findById(transporterId)
                .orElseThrow(() -> new ResourceNotFoundException("Transporter not found with id: " + transporterId));

        return new TransporterResponseDTO(
                transporter.getTransporterId(),
                transporter.getCompanyName(),
                transporter.getRating()
        );
    }

    @Override
    @Transactional
    public TransporterResponseDTO updateTrucks(UUID transporterId, UpdateTrucksRequestDTO request) {
        Transporter transporter = transporterRepository.findById(transporterId)
                .orElseThrow(() -> new ResourceNotFoundException("Transporter not found with id: " + transporterId));

        List<Truck> existingTrucks = truckRepository.findByTransporterTransporterId(transporterId);
        truckRepository.deleteAll(existingTrucks);

        for (UpdateTrucksRequestDTO.TruckDTO truckDTO : request.trucks()) {
            Truck truck = new Truck();
            truck.setTruckType(truckDTO.truckType());
            truck.setCount(truckDTO.count());
            truck.setTransporter(transporter);
            truckRepository.save(truck);
        }

        return new TransporterResponseDTO(
                transporter.getTransporterId(),
                transporter.getCompanyName(),
                transporter.getRating()
        );
    }
}
