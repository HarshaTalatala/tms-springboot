package com.harsha.tms.service.impl;

import java.time.Clock;
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
import com.harsha.tms.exception.ResourceNotFoundException;
import com.harsha.tms.repository.BidRepository;
import com.harsha.tms.repository.LoadRepository;
import com.harsha.tms.repository.TransporterRepository;
import com.harsha.tms.repository.TruckRepository;
import com.harsha.tms.service.BidService;
import com.harsha.tms.service.LoadStatusValidator;

@Service
public class BidServiceImpl implements BidService {

    private final BidRepository bidRepository;
    private final LoadRepository loadRepository;
    private final TransporterRepository transporterRepository;
    private final TruckRepository truckRepository;
    private final Clock clock;

    public BidServiceImpl(BidRepository bidRepository, LoadRepository loadRepository,
                          TransporterRepository transporterRepository, TruckRepository truckRepository, Clock clock) {
        this.bidRepository = bidRepository;
        this.loadRepository = loadRepository;
        this.transporterRepository = transporterRepository;
        this.truckRepository = truckRepository;
        this.clock = clock;
    }

    @Override
    @Transactional
    public BidResponseDTO submitBid(BidRequestDTO request) {
        Load load = loadRepository.findById(request.loadId())
                .orElseThrow(() -> new ResourceNotFoundException("Load not found with id: " + request.loadId()));

        LoadStatusValidator.validateStatusTransition(load.getStatus(), "BID");

        var transporter = transporterRepository.findById(request.transporterId())
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
        bid.setLoad(load);
        bid.setTransporter(transporter);
        bid.setProposedRate(request.proposedRate());
        bid.setTrucksOffered(request.trucksOffered());
        bid.setTruckType(request.truckType());
        bid.setStatus(BidStatus.PENDING);
        bid.setSubmittedAt(LocalDateTime.now(clock));

        Bid savedBid = bidRepository.save(bid);

        return new BidResponseDTO(
                savedBid.getBidId(),
                savedBid.getLoad().getId(),
                savedBid.getTransporter().getTransporterId(),
                savedBid.getProposedRate(),
                savedBid.getTrucksOffered(),
                savedBid.getTruckType(),
                savedBid.getStatus(),
                savedBid.getSubmittedAt()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<BidResponseDTO> listBids() {
        List<Bid> bids = bidRepository.findAll();

        return bids.stream()
                .map(bid -> new BidResponseDTO(
                        bid.getBidId(),
                        bid.getLoad().getId(),
                        bid.getTransporter().getTransporterId(),
                        bid.getProposedRate(),
                        bid.getTrucksOffered(),
                        bid.getTruckType(),
                        bid.getStatus(),
                        bid.getSubmittedAt()
                ))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public BidResponseDTO getBidById(UUID bidId) {
        Bid bid = bidRepository.findById(bidId)
                .orElseThrow(() -> new ResourceNotFoundException("Bid not found with id: " + bidId));

        return new BidResponseDTO(
                bid.getBidId(),
                bid.getLoad().getId(),
                bid.getTransporter().getTransporterId(),
                bid.getProposedRate(),
                bid.getTrucksOffered(),
                bid.getTruckType(),
                bid.getStatus(),
                bid.getSubmittedAt()
        );
    }

    @Override
    @Transactional
    public BidResponseDTO rejectBid(UUID bidId) {
        Bid bid = bidRepository.findById(bidId)
                .orElseThrow(() -> new ResourceNotFoundException("Bid not found with id: " + bidId));

        bid.setStatus(BidStatus.REJECTED);
        Bid savedBid = bidRepository.save(bid);

        return new BidResponseDTO(
                savedBid.getBidId(),
                savedBid.getLoad().getId(),
                savedBid.getTransporter().getTransporterId(),
                savedBid.getProposedRate(),
                savedBid.getTrucksOffered(),
                savedBid.getTruckType(),
                savedBid.getStatus(),
                savedBid.getSubmittedAt()
        );
    }
}
