package com.harsha.tms.service.impl;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.harsha.tms.dto.request.BidRequestDTO;
import com.harsha.tms.dto.request.BookingRequestDTO;
import com.harsha.tms.dto.request.LoadRequestDTO;
import com.harsha.tms.dto.response.BidResponseDTO;
import com.harsha.tms.dto.response.BookingResponseDTO;
import com.harsha.tms.dto.response.LoadResponseDTO;
import com.harsha.tms.entity.Bid;
import com.harsha.tms.entity.BidStatus;
import com.harsha.tms.entity.Booking;
import com.harsha.tms.entity.BookingStatus;
import com.harsha.tms.entity.Load;
import com.harsha.tms.entity.Truck;
import com.harsha.tms.exception.InsufficientCapacityException;
import com.harsha.tms.exception.InvalidStatusTransitionException;
import com.harsha.tms.exception.LoadAlreadyBookedException;
import com.harsha.tms.exception.ResourceNotFoundException;
import com.harsha.tms.repository.BidRepository;
import com.harsha.tms.repository.BookingRepository;
import com.harsha.tms.repository.LoadRepository;
import com.harsha.tms.repository.TransporterRepository;
import com.harsha.tms.repository.TruckRepository;

@Service
public class LoadServiceImpl implements com.harsha.tms.service.LoadService {

    private final LoadRepository loadRepository;
    private final BidRepository bidRepository;
    private final TransporterRepository transporterRepository;
    private final TruckRepository truckRepository;
    private final BookingRepository bookingRepository;

    public LoadServiceImpl(LoadRepository loadRepository, BidRepository bidRepository,
                           TransporterRepository transporterRepository, TruckRepository truckRepository,
                           BookingRepository bookingRepository) {
        this.loadRepository = loadRepository;
        this.bidRepository = bidRepository;
        this.transporterRepository = transporterRepository;
        this.truckRepository = truckRepository;
        this.bookingRepository = bookingRepository;
    }

    @Override
    public LoadResponseDTO createLoad(LoadRequestDTO request) {
        Load load = new Load();
        load.setPickupLocation(request.pickupLocation());
        load.setDeliveryLocation(request.deliveryLocation());
        load.setWeight(request.weight());
        load.setCargoType(request.cargoType());
        load.setPickupDate(request.pickupDate());
        load.setDeliveryDate(request.deliveryDate());
        load.setOfferedPrice(request.offeredPrice());
        load.setTrucksRequired(request.trucksRequired());
        load.setRemainingTrucks(request.trucksRequired());
        load.setStatus(BookingStatus.POSTED);
        load.setDatePosted(LocalDateTime.now());
        
        Load savedLoad = loadRepository.save(load);
        
        return new LoadResponseDTO(
                savedLoad.getId(),
                savedLoad.getPickupLocation(),
                savedLoad.getDeliveryLocation(),
                savedLoad.getWeight(),
                savedLoad.getCargoType(),
                savedLoad.getPickupDate(),
                savedLoad.getDeliveryDate(),
                savedLoad.getOfferedPrice(),
                savedLoad.getTrucksRequired(),
                savedLoad.getRemainingTrucks(),
                savedLoad.getStatus(),
                savedLoad.getDatePosted()
        );
    }

    @Override
    public LoadResponseDTO getLoadById(java.util.UUID loadId) {
        Load load = loadRepository.findById(loadId)
                .orElseThrow(() -> new ResourceNotFoundException("Load not found with id: " + loadId));
        
        return new LoadResponseDTO(
                load.getId(),
                load.getPickupLocation(),
                load.getDeliveryLocation(),
                load.getWeight(),
                load.getCargoType(),
                load.getPickupDate(),
                load.getDeliveryDate(),
                load.getOfferedPrice(),
                load.getTrucksRequired(),
                load.getRemainingTrucks(),
                load.getStatus(),
                load.getDatePosted()
        );
    }

    @Override
    public Page<LoadResponseDTO> listLoads(Pageable pageable) {
        Page<Load> page = loadRepository.findAll(pageable);
        
        return page.map(load -> new LoadResponseDTO(
                load.getId(),
                load.getPickupLocation(),
                load.getDeliveryLocation(),
                load.getWeight(),
                load.getCargoType(),
                load.getPickupDate(),
                load.getDeliveryDate(),
                load.getOfferedPrice(),
                load.getTrucksRequired(),
                load.getRemainingTrucks(),
                load.getStatus(),
                load.getDatePosted()
        ));
    }

    @Override
    public LoadResponseDTO cancelLoad(java.util.UUID loadId) {
        Load load = loadRepository.findById(loadId)
                .orElseThrow(() -> new ResourceNotFoundException("Load not found with id: " + loadId));
        
        if (load.getStatus() == BookingStatus.BOOKED) {
            throw new InvalidStatusTransitionException("Cannot cancel load with status BOOKED");
        }
        
        load.setStatus(BookingStatus.CANCELLED);
        Load savedLoad = loadRepository.save(load);
        
        return new LoadResponseDTO(
                savedLoad.getId(),
                savedLoad.getPickupLocation(),
                savedLoad.getDeliveryLocation(),
                savedLoad.getWeight(),
                savedLoad.getCargoType(),
                savedLoad.getPickupDate(),
                savedLoad.getDeliveryDate(),
                savedLoad.getOfferedPrice(),
                savedLoad.getTrucksRequired(),
                savedLoad.getRemainingTrucks(),
                savedLoad.getStatus(),
                savedLoad.getDatePosted()
        );
    }

    @Override
    public List<BidResponseDTO> getBestBids(java.util.UUID loadId) {
        loadRepository.findById(loadId)
                .orElseThrow(() -> new ResourceNotFoundException("Load not found with id: " + loadId));
        
        List<Bid> bids = bidRepository.findByLoadId(loadId);
        
        return bids.stream()
                .map(bid -> {
                    Double ratingObj = bid.getTransporter().getRating();
                    double rating = ratingObj != null ? ratingObj : 0.0;
                    Double proposedRateObj = bid.getProposedRate();
                    double proposedRate = proposedRateObj != null ? proposedRateObj : Double.MAX_VALUE;
                    double score = 0.7 * (1.0 / proposedRate) + 0.3 * (rating / 5.0);
                    return new BidWithScore(bid, score);
                })
                .sorted(Comparator.comparingDouble(BidWithScore::score).reversed())
                .map(bidWithScore -> new BidResponseDTO(
                        bidWithScore.bid().getBidId(),
                        bidWithScore.bid().getLoadId(),
                        bidWithScore.bid().getTransporterId(),
                        bidWithScore.bid().getProposedRate(),
                        bidWithScore.bid().getTrucksOffered(),
                        bidWithScore.bid().getTruckType(),
                        bidWithScore.bid().getStatus(),
                        bidWithScore.bid().getSubmittedAt()
                ))
                .toList();
    }
    
    private record BidWithScore(Bid bid, double score) {}

    @Override
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
    public BidResponseDTO rejectBid(java.util.UUID bidId) {
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

    @Override
    @Transactional
    public BookingResponseDTO createBooking(BookingRequestDTO request) {
        try {
            Load load = loadRepository.findById(request.loadId())
                    .orElseThrow(() -> new ResourceNotFoundException("Bid not found with id: " + request.loadId()));
            
            Bid bid = bidRepository.findById(request.bidId())
                    .orElseThrow(() -> new ResourceNotFoundException("Bid not found with id: " + request.bidId()));
            
            transporterRepository.findById(request.transporterId())
                    .orElseThrow(() -> new ResourceNotFoundException("Transporter not found with id: " + request.transporterId()));
            
            if (request.allocatedTrucks() > load.getRemainingTrucks()) {
                throw new InsufficientCapacityException("Insufficient remaining trucks. Requested: " + 
                        request.allocatedTrucks() + ", Available: " + load.getRemainingTrucks());
            }
            
            List<Truck> trucks = truckRepository.findByTransporterTransporterId(request.transporterId());
            for (Truck truck : trucks) {
                if (truck.getTruckType().equals(bid.getTruckType())) {
                    Integer currentCount = truck.getCount();
                    int available = currentCount != null ? currentCount : 0;
                    if (available >= request.allocatedTrucks()) {
                        truck.setCount(available - request.allocatedTrucks());
                        truckRepository.save(truck);
                        break;
                    }
                }
            }
            
            load.setRemainingTrucks(load.getRemainingTrucks() - request.allocatedTrucks());
            
            if (load.getRemainingTrucks() == 0) {
                load.setStatus(BookingStatus.BOOKED);
            }
            
            loadRepository.save(load);
            
            Booking booking = new Booking();
            booking.setLoadId(request.loadId());
            booking.setBidId(request.bidId());
            booking.setTransporterId(request.transporterId());
            booking.setAllocatedTrucks(request.allocatedTrucks());
            booking.setFinalRate(request.finalRate());
            booking.setStatus(BookingStatus.CONFIRMED);
            booking.setBookedAt(LocalDateTime.now());
            
            Booking savedBooking = bookingRepository.save(booking);
            
            bid.setStatus(BidStatus.ACCEPTED);
            bidRepository.save(bid);
            
            return new BookingResponseDTO(
                    savedBooking.getBookingId(),
                    savedBooking.getLoadId(),
                    savedBooking.getBidId(),
                    savedBooking.getTransporterId(),
                    savedBooking.getAllocatedTrucks(),
                    savedBooking.getFinalRate(),
                    savedBooking.getStatus(),
                    savedBooking.getBookedAt()
            );
        } catch (ObjectOptimisticLockingFailureException e) {
            throw new LoadAlreadyBookedException("Load was modified by another transaction. Please retry.");
        }
    }

    @Override
    @Transactional
    public BookingResponseDTO cancelBooking(java.util.UUID bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + bookingId));
        
        Load load = loadRepository.findById(booking.getLoadId())
                .orElseThrow(() -> new ResourceNotFoundException("Load not found with id: " + booking.getLoadId()));
        
        Bid bid = bidRepository.findById(booking.getBidId())
                .orElseThrow(() -> new ResourceNotFoundException("Bid not found with id: " + booking.getBidId()));
        
        List<Truck> trucks = truckRepository.findByTransporterTransporterId(booking.getTransporterId());
        for (Truck truck : trucks) {
            if (truck.getTruckType().equals(bid.getTruckType())) {
                Integer currentCount = truck.getCount();
                int available = currentCount != null ? currentCount : 0;
                truck.setCount(available + booking.getAllocatedTrucks());
                truckRepository.save(truck);
                break;
            }
        }
        
        boolean wasBooked = load.getStatus() == BookingStatus.BOOKED;
        load.setRemainingTrucks(load.getRemainingTrucks() + booking.getAllocatedTrucks());
        
        if (wasBooked && load.getRemainingTrucks().equals(load.getTrucksRequired())) {
            load.setStatus(BookingStatus.OPEN_FOR_BIDS);
        }
        
        loadRepository.save(load);
        
        booking.setStatus(BookingStatus.CANCELLED);
        Booking savedBooking = bookingRepository.save(booking);
        
        return new BookingResponseDTO(
                savedBooking.getBookingId(),
                savedBooking.getLoadId(),
                savedBooking.getBidId(),
                savedBooking.getTransporterId(),
                savedBooking.getAllocatedTrucks(),
                savedBooking.getFinalRate(),
                savedBooking.getStatus(),
                savedBooking.getBookedAt()
        );
    }
}
