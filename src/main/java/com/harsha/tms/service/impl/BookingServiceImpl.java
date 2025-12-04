package com.harsha.tms.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.harsha.tms.dto.request.BookingRequestDTO;
import com.harsha.tms.dto.response.BookingResponseDTO;
import com.harsha.tms.entity.Bid;
import com.harsha.tms.entity.BidStatus;
import com.harsha.tms.entity.Booking;
import com.harsha.tms.entity.BookingStatus;
import com.harsha.tms.entity.Load;
import com.harsha.tms.entity.Truck;
import com.harsha.tms.exception.InsufficientCapacityException;
import com.harsha.tms.exception.LoadAlreadyBookedException;
import com.harsha.tms.exception.ResourceNotFoundException;
import com.harsha.tms.repository.BidRepository;
import com.harsha.tms.repository.BookingRepository;
import com.harsha.tms.repository.LoadRepository;
import com.harsha.tms.repository.TransporterRepository;
import com.harsha.tms.repository.TruckRepository;
import com.harsha.tms.service.BookingService;

@Service
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final LoadRepository loadRepository;
    private final BidRepository bidRepository;
    private final TransporterRepository transporterRepository;
    private final TruckRepository truckRepository;

    public BookingServiceImpl(BookingRepository bookingRepository, LoadRepository loadRepository,
                              BidRepository bidRepository, TransporterRepository transporterRepository,
                              TruckRepository truckRepository) {
        this.bookingRepository = bookingRepository;
        this.loadRepository = loadRepository;
        this.bidRepository = bidRepository;
        this.transporterRepository = transporterRepository;
        this.truckRepository = truckRepository;
    }

    @Override
    @Transactional
    public BookingResponseDTO createBooking(BookingRequestDTO request) {
        try {
            Load load = loadRepository.findById(request.loadId())
                    .orElseThrow(() -> new ResourceNotFoundException("Load not found with id: " + request.loadId()));

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
    public BookingResponseDTO getBookingById(UUID bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + bookingId));

        return new BookingResponseDTO(
                booking.getBookingId(),
                booking.getLoadId(),
                booking.getBidId(),
                booking.getTransporterId(),
                booking.getAllocatedTrucks(),
                booking.getFinalRate(),
                booking.getStatus(),
                booking.getBookedAt()
        );
    }

    @Override
    @Transactional
    public BookingResponseDTO cancelBooking(UUID bookingId) {
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
