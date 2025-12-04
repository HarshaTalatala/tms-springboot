package com.harsha.tms.service;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.harsha.tms.dto.request.BookingRequestDTO;
import com.harsha.tms.dto.response.BookingResponseDTO;
import com.harsha.tms.entity.Bid;
import com.harsha.tms.entity.BidStatus;
import com.harsha.tms.entity.Booking;
import com.harsha.tms.entity.BookingStatus;
import com.harsha.tms.entity.Load;
import com.harsha.tms.entity.Transporter;
import com.harsha.tms.entity.Truck;
import com.harsha.tms.exception.InsufficientCapacityException;
import com.harsha.tms.exception.InvalidStatusTransitionException;
import com.harsha.tms.exception.ResourceNotFoundException;
import com.harsha.tms.repository.BidRepository;
import com.harsha.tms.repository.BookingRepository;
import com.harsha.tms.repository.LoadRepository;
import com.harsha.tms.repository.TransporterRepository;
import com.harsha.tms.repository.TruckRepository;
import com.harsha.tms.service.impl.BookingServiceImpl;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private LoadRepository loadRepository;

    @Mock
    private BidRepository bidRepository;

    @Mock
    private TransporterRepository transporterRepository;

    @Mock
    private TruckRepository truckRepository;

    @Mock
    private Clock clock;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private UUID testBookingId;
    private UUID testLoadId;
    private UUID testBidId;
    private UUID testTransporterId;
    private BookingRequestDTO bookingRequestDTO;
    private Booking booking;
    private Load load;
    private Bid bid;
    private Transporter transporter;
    private Truck truck;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        testBookingId = UUID.randomUUID();
        testLoadId = UUID.randomUUID();
        testBidId = UUID.randomUUID();
        testTransporterId = UUID.randomUUID();

        Clock fixedClock = Clock.fixed(Instant.parse("2024-01-01T10:00:00Z"), ZoneId.systemDefault());
        when(clock.instant()).thenReturn(fixedClock.instant());
        when(clock.getZone()).thenReturn(fixedClock.getZone());

        bookingRequestDTO = new BookingRequestDTO(
                testLoadId,
                testBidId,
                testTransporterId,
                2,
                5000.0
        );

        load = new Load();
        load.setId(testLoadId);
        load.setTrucksRequired(3);
        load.setRemainingTrucks(3);
        load.setStatus(BookingStatus.OPEN_FOR_BIDS);

        transporter = new Transporter();
        transporter.setTransporterId(testTransporterId);
        transporter.setCompanyName("Test Transporter");
        transporter.setRating(4.5);

        bid = new Bid();
        bid.setBidId(testBidId);
        bid.setLoad(load);
        bid.setTransporter(transporter);
        bid.setTruckType("Flatbed");
        bid.setProposedRate(5000.0);
        bid.setStatus(BidStatus.PENDING);

        truck = new Truck();
        truck.setTruckType("Flatbed");
        truck.setCount(5);
        truck.setTransporter(transporter);

        booking = new Booking();
        booking.setBookingId(testBookingId);
        booking.setLoad(load);
        booking.setBid(bid);
        booking.setTransporter(transporter);
        booking.setAllocatedTrucks(2);
        booking.setFinalRate(5000.0);
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setBookedAt(LocalDateTime.now(clock));
    }

    @Test
    void testCreateBooking_Success() {
        when(loadRepository.findById(testLoadId)).thenReturn(Optional.of(load));
        when(bidRepository.findById(testBidId)).thenReturn(Optional.of(bid));
        when(transporterRepository.findById(testTransporterId)).thenReturn(Optional.of(transporter));
        when(bidRepository.findByLoad_Id(testLoadId)).thenReturn(Arrays.asList(bid));
        when(truckRepository.findByTransporterTransporterId(testTransporterId)).thenReturn(Arrays.asList(truck));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        BookingResponseDTO response = bookingService.createBooking(bookingRequestDTO);

        assertNotNull(response);
        assertEquals(testBookingId, response.bookingId());
        assertEquals(testLoadId, response.loadId());
        assertEquals(testBidId, response.bidId());
        assertEquals(testTransporterId, response.transporterId());
        assertEquals(2, response.allocatedTrucks());
        assertEquals(5000.0, response.finalRate());
        assertEquals(BookingStatus.CONFIRMED, response.status());
        verify(bookingRepository, times(1)).save(any(Booking.class));
        verify(truckRepository, times(1)).save(any(Truck.class));
    }

    @Test
    void testCreateBooking_LoadNotFound() {
        when(loadRepository.findById(testLoadId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> bookingService.createBooking(bookingRequestDTO));
        assertNotNull(exception);
        verify(loadRepository, times(1)).findById(testLoadId);
    }

    @Test
    void testCreateBooking_BidNotFound() {
        when(loadRepository.findById(testLoadId)).thenReturn(Optional.of(load));
        when(bidRepository.findById(testBidId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> bookingService.createBooking(bookingRequestDTO));
        assertNotNull(exception);
        verify(bidRepository, times(1)).findById(testBidId);
    }

    @Test
    void testCreateBooking_TransporterNotFound() {
        when(loadRepository.findById(testLoadId)).thenReturn(Optional.of(load));
        when(bidRepository.findById(testBidId)).thenReturn(Optional.of(bid));
        when(transporterRepository.findById(testTransporterId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> bookingService.createBooking(bookingRequestDTO));
        assertNotNull(exception);
        verify(transporterRepository, times(1)).findById(testTransporterId);
    }

    @Test
    void testCreateBooking_AcceptedBidAlreadyExists() {
        Bid acceptedBid = new Bid();
        acceptedBid.setStatus(BidStatus.ACCEPTED);

        when(loadRepository.findById(testLoadId)).thenReturn(Optional.of(load));
        when(bidRepository.findById(testBidId)).thenReturn(Optional.of(bid));
        when(transporterRepository.findById(testTransporterId)).thenReturn(Optional.of(transporter));
        when(bidRepository.findByLoad_Id(testLoadId)).thenReturn(Arrays.asList(acceptedBid));

        InvalidStatusTransitionException exception = assertThrows(InvalidStatusTransitionException.class,
                () -> bookingService.createBooking(bookingRequestDTO));
        assertNotNull(exception);
    }

    @Test
    void testCreateBooking_InsufficientRemainingTrucks() {
        load.setRemainingTrucks(1);
        BookingRequestDTO request = new BookingRequestDTO(testLoadId, testBidId, testTransporterId, 2, 5000.0);

        when(loadRepository.findById(testLoadId)).thenReturn(Optional.of(load));
        when(bidRepository.findById(testBidId)).thenReturn(Optional.of(bid));
        when(transporterRepository.findById(testTransporterId)).thenReturn(Optional.of(transporter));
        when(bidRepository.findByLoad_Id(testLoadId)).thenReturn(Arrays.asList(bid));

        InsufficientCapacityException exception = assertThrows(InsufficientCapacityException.class,
                () -> bookingService.createBooking(request));
        assertNotNull(exception);
    }

    @Test
    void testCreateBooking_InsufficientTrucksOfType() {
        truck.setCount(1);

        when(loadRepository.findById(testLoadId)).thenReturn(Optional.of(load));
        when(bidRepository.findById(testBidId)).thenReturn(Optional.of(bid));
        when(transporterRepository.findById(testTransporterId)).thenReturn(Optional.of(transporter));
        when(bidRepository.findByLoad_Id(testLoadId)).thenReturn(Arrays.asList(bid));
        when(truckRepository.findByTransporterTransporterId(testTransporterId)).thenReturn(Arrays.asList(truck));

        InsufficientCapacityException exception = assertThrows(InsufficientCapacityException.class,
                () -> bookingService.createBooking(bookingRequestDTO));
        assertNotNull(exception);
    }

    @Test
    void testGetBookingById_Success() {
        when(bookingRepository.findById(testBookingId)).thenReturn(Optional.of(booking));

        BookingResponseDTO response = bookingService.getBookingById(testBookingId);

        assertNotNull(response);
        assertEquals(testBookingId, response.bookingId());
        assertEquals(testLoadId, response.loadId());
        verify(bookingRepository, times(1)).findById(testBookingId);
    }

    @Test
    void testGetBookingById_NotFound() {
        when(bookingRepository.findById(testBookingId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> bookingService.getBookingById(testBookingId));
        assertNotNull(exception);
        verify(bookingRepository, times(1)).findById(testBookingId);
    }

    @Test
    void testCancelBooking_Success() {
        load.setStatus(BookingStatus.OPEN_FOR_BIDS);
        load.setRemainingTrucks(1);
        booking.setStatus(BookingStatus.CONFIRMED);

        when(bookingRepository.findById(testBookingId)).thenReturn(Optional.of(booking));
        when(truckRepository.findByTransporterTransporterId(testTransporterId)).thenReturn(Arrays.asList(truck));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        BookingResponseDTO response = bookingService.cancelBooking(testBookingId);

        assertNotNull(response);
        assertEquals(BookingStatus.CANCELLED, response.status());
        verify(bookingRepository, times(1)).findById(testBookingId);
        verify(bookingRepository, times(1)).save(any(Booking.class));
        verify(truckRepository, times(1)).save(any(Truck.class));
    }

    @Test
    void testCancelBooking_NotFound() {
        when(bookingRepository.findById(testBookingId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> bookingService.cancelBooking(testBookingId));
        assertNotNull(exception);
        verify(bookingRepository, times(1)).findById(testBookingId);
    }

    @Test
    void testCancelBooking_ResetsLoadStatusWhenFullyAvailable() {
        load.setStatus(BookingStatus.BOOKED);
        load.setTrucksRequired(3);
        load.setRemainingTrucks(1);
        booking.setAllocatedTrucks(2);

        when(bookingRepository.findById(testBookingId)).thenReturn(Optional.of(booking));
        when(truckRepository.findByTransporterTransporterId(testTransporterId)).thenReturn(Arrays.asList(truck));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        bookingService.cancelBooking(testBookingId);

        verify(loadRepository, times(1)).save(any(Load.class));
    }
}
