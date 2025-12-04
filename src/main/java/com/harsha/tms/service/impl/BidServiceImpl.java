package com.harsha.tms.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.harsha.tms.dto.request.BidRequestDTO;
import com.harsha.tms.dto.response.BidResponseDTO;
import com.harsha.tms.entity.Bid;
import com.harsha.tms.entity.BidStatus;
import com.harsha.tms.entity.BookingStatus;
import com.harsha.tms.entity.Load;
import com.harsha.tms.entity.Truck;
import com.harsha.tms.exception.InsufficientCapacityException;
import com.harsha.tms.exception.InvalidStatusTransitionException;
import com.harsha.tms.exception.ResourceNotFoundException;
import com.harsha.tms.repository.BidRepository;
import com.harsha.tms.repository.LoadRepository;
import com.harsha.tms.repository.TransporterRepository;
import com.harsha.tms.repository.TruckRepository;
import com.harsha.tms.service.BidService;

@Service
public class BidServiceImpl implements BidService {

    private final BidRepository bidRepository;
    private final LoadRepository loadRepository;
    private final TransporterRepository transporterRepository;
    private final TruckRepository truckRepository;

    public BidServiceImpl(BidRepository bidRepository, LoadRepository loadRepository,
                          TransporterRepository transporterRepository, TruckRepository truckRepository) {
        this.bidRepository = bidRepository;
        this.loadRepository = loadRepository;
        this.transporterRepository = transporterRepository;
        this.truckRepository = truckRepository;
    }

    @Override
    @Transactional
    public BidResponseDTO submitBid(BidRequestDTO request) {
        Load load = loadRepository.findById(request.loadId())
                .orElseThrow(() -> new ResourceNotFoundException("Load not found with id: " + request.loadId()));

        if (load.getStatus() == BookingStatus.BOOKED || load.getStatus() == BookingStatus.CANCELLED) {
            throw new InvalidStatusTransitionException("Cannot bid on load with status: " + load.getStatus());
        }

        transporterRepository.findById(request.transporterId())
                .orElseThrow(() -> new ResourceNotFoundException("Transporter not found with id: " + request.transporterId()));

        List<Truck> trucks = truckRepository.findByTransporterTransporterId(request.transporterId());
        int availableTrucks = trucks.stream()
                .filter(truck -> truck.getTruckType().equals(request.truckType()))
                .mapToInt(truck -> {
                    Integer count = truck.getCount();
                    return count != null ? count : 0;
                })
                .sum();

        if (request.trucksOffered() > availableTrucks) {
            throw new InsufficientCapacityException("Insufficient trucks available. Requested: " +
                    request.trucksOffered() + ", Available: " + availableTrucks);
        }

        if (load.getStatus() == BookingStatus.POSTED) {
            List<Bid> existingBids = bidRepository.findByLoadId(request.loadId());
            if (existingBids.isEmpty()) {
                load.setStatus(BookingStatus.OPEN_FOR_BIDS);
                loadRepository.save(load);
            }
        }

        Bid bid = new Bid();
        bid.setLoadId(request.loadId());
        bid.setTransporterId(request.transporterId());
        bid.setProposedRate(request.proposedRate());
        bid.setTrucksOffered(request.trucksOffered());
        bid.setTruckType(request.truckType());
        bid.setStatus(BidStatus.PENDING);
        bid.setSubmittedAt(LocalDateTime.now());

        Bid savedBid = bidRepository.save(bid);

        return new BidResponseDTO(
                savedBid.getBidId(),
                savedBid.getLoadId(),
                savedBid.getTransporterId(),
                savedBid.getProposedRate(),
                savedBid.getTrucksOffered(),
                savedBid.getTruckType(),
                savedBid.getStatus(),
                savedBid.getSubmittedAt()
        );
    }

    @Override
    public List<BidResponseDTO> listBids() {
        List<Bid> bids = bidRepository.findAll();

        return bids.stream()
                .map(bid -> new BidResponseDTO(
                        bid.getBidId(),
                        bid.getLoadId(),
                        bid.getTransporterId(),
                        bid.getProposedRate(),
                        bid.getTrucksOffered(),
                        bid.getTruckType(),
                        bid.getStatus(),
                        bid.getSubmittedAt()
                ))
                .toList();
    }

    @Override
    public BidResponseDTO getBidById(UUID bidId) {
        Bid bid = bidRepository.findById(bidId)
                .orElseThrow(() -> new ResourceNotFoundException("Bid not found with id: " + bidId));

        return new BidResponseDTO(
                bid.getBidId(),
                bid.getLoadId(),
                bid.getTransporterId(),
                bid.getProposedRate(),
                bid.getTrucksOffered(),
                bid.getTruckType(),
                bid.getStatus(),
                bid.getSubmittedAt()
        );
    }

    @Override
    public BidResponseDTO rejectBid(UUID bidId) {
        Bid bid = bidRepository.findById(bidId)
                .orElseThrow(() -> new ResourceNotFoundException("Bid not found with id: " + bidId));

        bid.setStatus(BidStatus.REJECTED);
        Bid savedBid = bidRepository.save(bid);

        return new BidResponseDTO(
                savedBid.getBidId(),
                savedBid.getLoadId(),
                savedBid.getTransporterId(),
                savedBid.getProposedRate(),
                savedBid.getTrucksOffered(),
                savedBid.getTruckType(),
                savedBid.getStatus(),
                savedBid.getSubmittedAt()
        );
    }
}
